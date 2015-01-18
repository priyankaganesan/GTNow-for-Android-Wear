package edu.gatech.seclass.GTNow;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.app.ListActivity;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import java.util.concurrent.ExecutionException;
import android.content.res.AssetManager;


public class Main extends ListActivity {

    //private static String filePath = "json.txt";
    private static String fileContents;

    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";
    private static final String INSTRUCTIONS = "html_instructions";
    private static final String Steps = "steps";

    JSONArray jsonDistance;
    JSONArray jsonDuration;
    JSONArray jsonDirections;
    JSONArray jsonSteps;

    ArrayList<String> distances;
    ArrayList<String> durations;
    ArrayList<String> directions;
    ArrayList<String> steps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
//        MapsActivity.MessageReceiver messageReceiver = new MapsActivity.MessageReceiver();
//        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        //AssetManager assetManager = getAssets();
        distances = new ArrayList<String>();
        durations = new ArrayList<String>();
        directions = new ArrayList<String>();
        steps = new ArrayList<String>();
        ListView lv = getListView();
        //try {
//            InputStream input = assetManager.open("json.txt");
//
//            int size = input.available();
//            byte[] buffer = new byte[size];
//            input.read(buffer);
//            input.close();

            // byte buffer into a string
            //fileContents = new String(buffer);
            //getData();
        //}
        //catch (Exception ex){
        //}
        String test="test";

    }

    public void getData(String json){
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONArray jsonRoutes = jsonObj.getJSONArray("routes");
            JSONArray jsonLegs = jsonRoutes.getJSONObject(0).getJSONArray("legs");
            jsonSteps = jsonLegs.getJSONObject(0).getJSONArray("steps");
            for(int index = 0; index < jsonSteps.length(); index++){
                JSONObject c = jsonSteps.getJSONObject(index);
                String distance = c.getJSONObject(DISTANCE).getString("text");
                String duration = c.getJSONObject(DURATION).getString("text");
                String direction = c.getString(INSTRUCTIONS);
                direction = direction.replaceAll("<[^>]*>", "");
                distances.add(distance);
                durations.add(duration);
                directions.add(direction);
            }
            notification_stack();
        }
        catch (Exception ex){
            Exception x = ex;
            String exp = x.toString();
        }
    }

    public void notification_stack()
    {
        final String GROUP_DIRECTIONS = "group_directions";

        for(int i=0;i<14;i++) {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            // Build the notification, setting the group appropriately
            Notification notif = new NotificationCompat.Builder(this)
                    .setContentTitle("Distance" + distances.get(i))
                    .setContentText("Duration:" + durations.get(i))
                    .setContentText("Directions:" + directions.get(i))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setGroup(GROUP_DIRECTIONS)
                    .build();

            notificationManager.notify(i, notif);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}


