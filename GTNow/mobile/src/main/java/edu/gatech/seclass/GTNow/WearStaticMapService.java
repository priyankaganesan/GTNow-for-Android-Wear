//Priyanka's Static Map API key: AIzaSyD0P6HWTzVC-RXtf8YcS7CDEjihboEA_9M
package edu.gatech.seclass.GTNow;

/**
 * Created by Priyanka on 11/26/2014.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dheera Venkatraman
 * http://dheera.net
 */
public class WearStaticMapService extends WearableListenerService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "WearMaps/" + String.valueOf((new Random()).nextInt(10000));
    private static final boolean D = true;

    private static GoogleApiClient mGoogleApiClient = null;
    private static LocationClient mLocationClient = null;
    private static LocationRequest mLocationRequest = null;
    private static Node mWearableNode = null;

    private long lastPingTime = 0;

    private static void sendToWearable(String path, byte[] data, final ResultCallback<MessageApi.SendMessageResult> callback) {
        if (mWearableNode != null) {
            PendingResult<MessageApi.SendMessageResult> pending = Wearable.MessageApi.sendMessage(mGoogleApiClient, mWearableNode.getId(), path, data);
            pending.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult result) {
                    if (callback != null) {
                        callback.onResult(result);
                    }
                    if (!result.getStatus().isSuccess()) {
                        if(D) Log.d(TAG, "ERROR: failed to send Message: " + result.getStatus());
                    }
                }
            });
        } else {
            if(D) Log.d(TAG, "ERROR: tried to send message before device was found");
        }
    }

    void findWearableNodeAndBlock() {
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                if(result.getNodes().size()>0) {
                    mWearableNode = result.getNodes().get(0);
                    if(D) Log.d(TAG, "Found wearable: name=" + mWearableNode.getDisplayName() + ", id=" + mWearableNode.getId());
                } else {
                    mWearableNode = null;
                }
            }
        });
        int i = 0;
        while(mWearableNode == null && i++<50) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e ) {
                // don't care
            }
        }
    }

    private void onMessageStart() {
        Log.d(TAG, "onMessageStart");

        Notification note=new Notification(R.drawable.ic_launcher,
                "Wear Maps is active",
                System.currentTimeMillis());
        Intent i=new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(this, 0,
                i, 0);

        note.setLatestEventInfo(this, "Wear Maps",
                "Wear Maps is active",
                pi);
        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(1337, note);
    }

    private void onMessagePing() {
        lastPingTime = System.currentTimeMillis();
    }

    private void onMessageStop() {
        Log.d(TAG, "onMessageStop");
        if(mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
        stopForeground(true);
    }

    protected static void onMessageLocate(ArrayList<GroupDetailsObject> coords) {
        final String maptype = "roadmap";
        final String format = "jpg";
        final int googleZoom = 16; //default value used by dheera sample code
        InputStream is;
        final boolean group_flag;
        if(coords == null)
            group_flag = true;
        else
            group_flag = false;



        Log.d(TAG, "onMessageLocate");

        if (mLocationClient != null && mLocationClient.isConnected()) {
            Location location = mLocationClient.getLastLocation();
            if (location == null) {
                if (D) Log.d(TAG, "No location available");
            } else {
                if (D) {
                    final double latitude   = location.getLatitude();
                    final double longitude  = location.getLongitude();
                    final double accuracy   = location.getAccuracy();
                    Log.d(TAG, String.format("Got location: %f %f %f", latitude, longitude, accuracy));
                    final String url;
                    if(coords == null){
                        url = String.format(
                                "http://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=256x282&maptype=%s&format=%s",
                                latitude, longitude, googleZoom, maptype, format);
                    } else{
                        String base_url   = "https://maps.googleapis.com/maps/api/staticmap?size=256x282";
                        String self_mark  = "&markers=color:red%7Clabel:A%7C"+latitude+","+longitude;

                        String group_base = "&markers=color:blue";
                        final String sep  = "%7C";
                        StringBuilder group = new StringBuilder();
                        //size:tiny or small may be better for display

                        for(int i=0; i<coords.size(); i++){
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
                                    if (D) Log.d(TAG, String.format("read %d bytes", outdata.length));

                                    if(!group_flag)
                                        sendToWearable(String.format("map %f %f %d", latitude, longitude, googleZoom), outdata, null);
                                    else
                                        sendToWearable("group", outdata, null);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "onMessageGet: exception:", e);
                            }
                        }
                    }).start();
                    // we don't send location back; we send map
                    //sendToWearable(String.format("location %f %f", location.getLatitude(), location.getLongitude()), null, null);
                }
            }
        }
    }

    // this method isn't useful because we're not requesting multiple tiles. we only request a single one and pass it along. x,y are points specified
    // on the wear to recenter the image, so the wear has to request a tile update (wear uses x,y to compute new lat/longs; this method doesn't
    // use the x,y at all)


    private void onMessageGet(final int y, final int x, final double latitude, final double longitude) {
        if(D) Log.d(TAG, String.format("onMessageGet(%d, %d %f, %f)", x,y, latitude, longitude));

        final String maptype = "roadmap";
        final String format = "jpg";

//        InputStream is;
        //https://maps.googleapis.com/maps/api/staticmap?size=256x282&markers=color:red%7Clabel:S%7C33.777252,-84.396185%7C33.777362,-84.390098%7C33.775741,-84.405314%7C33.772356,-84.394838
        final String url = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?size=256x282&maptype=%s&format=%s&markers=color:red|label:S|%f,%f",maptype, format, latitude, longitude);

        if(D) Log.d(TAG, "onMessageGet: url: " + url);

        new Thread(new Runnable() {
            public void run() {
                try {
                    // download the image and send it to the wearable
                    try {
                        byte[] outdata = downloadUrl2(url);
                        if(D) Log.d(TAG, String.format("read %d bytes", outdata.length));
                        sendToWearable(String.format("response %d %d %f %f", y, x, latitude, longitude), outdata, null);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "onMessageGet: exception:", e);
                }
            }
        }).start();


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

    private byte[] downloadUrl(URL toDownload) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] chunk = new byte[16384];
            int bytesRead;
            InputStream stream = toDownload.openStream();

            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream.toByteArray();
    }

    @Override
    public void onCreate() {
        if(D) Log.d(TAG, "onCreate");
        super.onCreate();

        try {
            Class.forName("android.os.AsyncTask");
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(D) Log.d(TAG, "onDestroy");
        if(mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
        super.onDestroy();
    }

    @Override
    public void onPeerConnected(Node peer) {
        if(D) Log.d(TAG, "onPeerConnected");
        super.onPeerConnected(peer);
        if(D) Log.d(TAG, "Connected: name=" + peer.getDisplayName() + ", id=" + peer.getId());
    }

    @Override
    public void onMessageReceived(MessageEvent m) {
        if(D) Log.d(TAG, "onMessageReceived");
        if(D) Log.d(TAG, "path: " + m.getPath());
        if(D) Log.d(TAG, "data bytes: " + m.getData().length);

        Scanner scanner = new Scanner(m.getPath());
        String requestType = scanner.next();

        if (D) Log.d(TAG, "requestType: " + requestType);

        if (requestType.equals("stop")) {
            onMessageStop();
            return;
        }

        if(mGoogleApiClient == null) {
            if(D) Log.d(TAG, "setting up GoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addApi(LocationServices.API)
                    .build();
            if(D) Log.d(TAG, "connecting to GoogleApiClient");
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, String.format("GoogleApiClient connect failed with error code %d", connectionResult.getErrorCode()));
                return;
            } else {
                if(D) Log.d(TAG, "GoogleApiClient connect success, finding wearable node");
                findWearableNodeAndBlock();
                if(D) Log.d(TAG, "wearable node found");
            }
        } else if(mWearableNode == null) {
            if(D) Log.d(TAG, "GoogleApiClient was connected but wearable not found, finding wearable node");
            findWearableNodeAndBlock();
            if(mWearableNode == null) {
                if(D) Log.d(TAG, "wearable node not found");
                return;
            }
        }

        if(mLocationClient == null) {
            if (mLocationClient == null) mLocationClient = new LocationClient(this, this, this);
        }

        if(!mLocationClient.isConnected()) {
            mLocationClient.connect();
            while(mLocationClient.isConnecting()) {
                try {
                    Thread.sleep(50);
                } catch(InterruptedException e) { e.printStackTrace(); }
            }
        }

        if (requestType.equals("get")) {
            if (!scanner.hasNextInt()) {
                if (D) Log.d(TAG, "invalid message parameter");
                return;
            }
            int y = scanner.nextInt();
            if (!scanner.hasNextInt()) {
                if (D) Log.d(TAG, "invalid message parameter");
                return;
            }
            int x = scanner.nextInt();
            if (!scanner.hasNextDouble()) {
                if (D) Log.d(TAG, "invalid message parameter");
                return;
            }
            double latitude = scanner.nextDouble();
            if (!scanner.hasNextDouble()) {
                if (D) Log.d(TAG, "invalid message parameter");
                return;
            }
            double longitude = scanner.nextDouble();
            if (!scanner.hasNextInt()) {
                if (D) Log.d(TAG, "invalid message parameter");
                return;
            }
            onMessageGet(y, x, latitude, longitude);
        }
        if (requestType.equals("locate")) {
            onMessageLocate(null);
        }
        if (requestType.equals("start")) {
            onMessageStart();
        }
        if (requestType.equals("ping")) {
            onMessagePing();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // don't care
    }

    // when location is changed, we send an updated map to the wearable by calling the modified onMessageLocate() again
    @Override
    public void onLocationChanged(Location location) {
        if(D) Log.d(TAG, String.format("received location: %f %f %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
//        sendToWearable(String.format("location %f %f", location.getLatitude(), location.getLongitude()), null, null);
        onMessageLocate(null);

        if (System.currentTimeMillis() - lastPingTime > 15000) {
            Log.d(TAG, String.format("ping timeout %d ms, disconnecting", System.currentTimeMillis() - lastPingTime));
            mLocationClient.removeLocationUpdates(this);
            mLocationClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }
    @Override
    public void onDisconnected() {
        mLocationClient = null;
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(D) Log.d(TAG, "connection failed");
        if (connectionResult.hasResolution()) {
            if(D) Log.d(TAG, "has resolution");
        } else {
            if(D) Log.d(TAG, "no resolution");
        }
    }
}