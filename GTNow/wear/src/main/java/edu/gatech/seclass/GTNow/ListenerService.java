package edu.gatech.seclass.GTNow;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ListenerService extends WearableListenerService {

    private static final String GROUPS = "/groups";

    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";
    private static final String INSTRUCTIONS = "html_instructions";

    //Priyanka
    private static final String NOTIFICATION = "/notification";
    private String notification_text;
    private String jsonDirections;
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DIRECTIONS_PATH = "/directions";
    private static final String DIRECTIONS_JSON = "directions_json";
    private static final String MAP = "/map";

    private ArrayList<String> distances;
    private ArrayList<String> durations;
    private ArrayList<String> directions;
    private int no_of_directions;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY_PATH ) ) {

            Log.d("TAG","Inside start activity");
            showToast("hahaha");
            createWearNotification();
        }  else if( messageEvent.getPath().equalsIgnoreCase( NOTIFICATION ) ) {
            Log.i("GTNow", "Inside notification");
            try {
                notification_text = new String(messageEvent.getData(), "UTF-8");
                createWearNotification();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else if( messageEvent.getPath().equalsIgnoreCase( DIRECTIONS_PATH ) ) {
            try {
                jsonDirections=new String(messageEvent.getData(), "UTF-8");
                parseDirections(jsonDirections);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //Priyanka: Handling incoming notification
        }
        else if (messageEvent.getPath().equalsIgnoreCase(GROUPS)){
            org.json.JSONObject jsonObj = null;
            try {
                jsonObj = new org.json.JSONObject(new String(messageEvent.getData()));
                org.json.JSONArray jsonGroups = jsonObj.getJSONArray("groups");
                HashMap<String, String> hashMap = new HashMap<String, String>(jsonGroups.length() * 2);
                for(int index = 0; index < jsonGroups.length(); index++) {
                    org.json.JSONObject c = jsonGroups.getJSONObject(index);
                    String id = c.getString("group_id");
                    String name = c.getString("group_name");

                    hashMap.put("group_id_" + index, id);
                    hashMap.put("group_name_" + index, name);
                }
//                DataHolder.getInstance().setData(hashMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (messageEvent.getPath().equalsIgnoreCase(MAP)){
            onMessageMap(messageEvent.getData());

        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void createWearNotification()
    {
        int notificationId = 001;

        Intent viewIntent = new Intent(this, MapsActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GTNOW ALERT ")
                        .setContentText("30 Minutes to Class")
                        .setContentIntent(viewPendingIntent);
        //Priyanka. Not applicable for wear?
        //.setExtras(extras);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void parseDirections(String json) {
        try {
            //Priyanka: Moving as member variable
            distances = new ArrayList<String>();
            durations = new ArrayList<String>();
            directions = new ArrayList<String>();

            JSONParser jsonParser = new JSONParser();
            json.replace("\\\"", "");

            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);

            String jsonString1 = (String) jsonObject.get("data");

            JSONObject jsonDataObject = (JSONObject) jsonParser.parse(jsonString1);
            JSONArray jsonArrayRoutes = (JSONArray) jsonDataObject.get("routes");

            JSONObject jsonRoutesObject = (JSONObject) jsonArrayRoutes.get(0);
            JSONArray jsonArrayLegs = (JSONArray) jsonRoutesObject.get("legs");
            JSONObject jsonLegsObject = (JSONObject) jsonArrayLegs.get(0);
            JSONArray jsonArraySteps = (JSONArray) jsonLegsObject.get("steps");
            no_of_directions=jsonArraySteps.size();
            for (int i = 0; i < jsonArraySteps.size(); i++) {
                JSONObject jsonStepsObject = (JSONObject) jsonArraySteps.get(i);

                //Priyanka: JSON parsing for wear
                JSONObject jsonDistance = (JSONObject) jsonStepsObject.get("distance");
                String dist=(String) jsonDistance.get("text");
                JSONObject jsonDuration = (JSONObject) jsonStepsObject.get("duration");
                String duration=(String) jsonDuration.get("text");
                String dir = (String) jsonStepsObject.get("html_instructions");
//                String dir = direction.toString() + "";
                dir = dir.replaceAll("<[^>]*>", "");
                distances.add(dist);
                durations.add(duration);
                directions.add(dir);

            }
            createNotificationStack();
        }
        catch (Exception ex){
            Exception x = ex;
            String exp = x.toString();
        }

    }

    public void createNotificationStack() {
        final String GROUP_DIRECTIONS = "group_directions";

        for(int i = no_of_directions - 1; i >= 0; i--) {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            // Build the notification, setting the group appropriately
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("In " + distances.get(i) + " (" + durations.get(i) + ")")
                    .setContentText( directions.get(i))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setGroup(GROUP_DIRECTIONS)
                    .build();

            notificationManager.notify(no_of_directions - i, notification);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void onMessageMap( byte[] data) {

        Intent intent = new Intent(ListenerService.this, ImageViewerActivity.class);
        intent.putExtra("imageBytes", data);
        startActivity(intent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/image")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset profileAsset = dataMapItem.getDataMap().getAsset("mapImage");
                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                Intent intent = new Intent(ListenerService.this, ImageViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("mapImage", bitmap);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                // Do something with the bitmap
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        ConnectionResult result =
                mGoogleApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w("GTNow", "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
