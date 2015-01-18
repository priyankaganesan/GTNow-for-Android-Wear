package edu.gatech.seclass.GTNow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class WearListenerService extends WearableListenerService {

    private static final String FIND_FRIENDS = "find_friends";
    private static final String DIRECTIONS = "directions";
    private static final String START = "start";
    private static final String LOCATE = "locate";

    //final WearCommunicator wearCommunicator = new WearCommunicator(this);

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.i("Wear Listener", messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase( FIND_FRIENDS ) ) {
            showToast("Message from wear");
            //Message from wear received here. Call the method for the phone team.
            String groupId = new String(messageEvent.getData());
            WebServerHelper.getGroups(groupId);
//            Intent intent = new Intent( this, MapsActivity.class );
//            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
//            startActivity( intent );
        } else if( messageEvent.getPath().equalsIgnoreCase( DIRECTIONS ) ) {
//            showToast("Message from wear");
            //Message from wear received here. Call the method for the phone team.
//            String test = WebServerHelper.jsonResponse;
            Log.i("JSONResponse", "Sending JSON response with directions");
            WearCommunicator wearCommunicator = new WearCommunicator(this);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wearCommunicator.sendDirections(WebServerHelper.jsonResponse);
            if(WebServerHelper.jsonResponse != null) {
                showToast("Directions sent to wear");
            }
        } else if( messageEvent.getPath().equalsIgnoreCase( START ) ) {

        } else if( messageEvent.getPath().equalsIgnoreCase( LOCATE ) ) {
            onMessageLocate(null);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static void onMessageLocate(ArrayList<GroupDetailsObject> coords) {

        final String maptype = "roadmap";
        final String format = "jpg";
        final int googleZoom = 16; //default value used by dheera sample code

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(GTNowApplication.getAppContext())
                .addApi(LocationServices.API)
                .build();

        Location location = null;
        if (mGoogleApiClient.blockingConnect().isSuccess()) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }

        final double latitude   = 33.7759 + 0.0001 * Math.random();// .7759491;//,location.getLatitude();
        final double longitude  = -84.3971 + 0.0001 * Math.random();//.3971271;//location.getLongitude();
        final String url;
        if(coords == null) {
            String tempUrl;
            if(location != null)  {
                tempUrl = String.format(
                        "http://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=256x282&maptype=%s&format=%s",
                        location.getLatitude(), location.getLongitude(), googleZoom, maptype, format);
                url = tempUrl + "&markers=color:blue%7Clabel:A%7C"+location.getLatitude()+","+location.getLongitude();
            } else {
                tempUrl = String.format(
                        "http://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=256x282&maptype=%s&format=%s",
                        latitude, longitude, googleZoom, maptype, format);
                url = tempUrl + "&markers=color:blue%7Clabel:A%7C"+latitude+","+longitude;
            }
        } else {
            String[] colors = {"red", "blue", "green", "yellow", "brown", "orange", "purple", "black", "white", "gray"};
            String base_url   = "https://maps.googleapis.com/maps/api/staticmap?size=256x282";
            String self_mark  = "&markers=color:"+colors[(int)(Math.random() * colors.length)]+"%7Clabel:A%7C"+latitude+","+longitude;

            String group_base = "&markers=color:"+colors[(int)(Math.random() * colors.length)];
            final String sep  = "%7C";
            StringBuilder group = new StringBuilder();
            //size:tiny or small may be better for display

            for(int i = 0; i < coords.size(); i++){
                GroupDetailsObject g = coords.get(i);
                group.append(group_base);
                group.append(sep);
                group.append("label:");
                group.append(i+1); //group members will be labeled 1, 2, 3, 4, etc.
                group.append(sep);
                group.append(g.latitude+","+g.longitude);
            }

            url = base_url+self_mark+group.toString();
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    // download the image and send it to the wearable
                    try {
                        byte[] outdata = downloadUrl2(url);
                        Bitmap bm = BitmapFactory.decodeByteArray(outdata, 0, outdata.length);
                        WearCommunicator wearCommunicator = new WearCommunicator(GTNowApplication.getAppContext());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        wearCommunicator.sendMap(bm);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e("GTNow", "Exception inside maps thread", e);
                }
            }
        }).start();
        // we don't send location back; we send map
        //sendToWearable(String.format("location %f %f", location.getLatitude(), location.getLongitude()), null, null);
//                }
//            }
//        }
    }

    private static byte[] downloadUrl2(String url) {
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpclient = new DefaultHttpClient();
        byte[] content;
        try {
            HttpResponse response = httpclient.execute(httpGet);
            return EntityUtils.toByteArray(response.getEntity());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
