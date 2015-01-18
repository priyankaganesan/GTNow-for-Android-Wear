package edu.gatech.seclass.GTNow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;


public class EventActivity extends Activity {
    public static double latitude;
    public static double longitude;
    Button getRoute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        //Bundle bundle = getIntent().getExtras();

        try {
            Bundle bundle = WebServerHelper.eventExtras;

            String event_name="";
            String event_time="";
            String event_location="";
            if (bundle != null) {
                System.out.println("Bundle received:"+ bundle.toString());

                String action = bundle.getString("action", "default");
                System.out.println("Action received:"+ action);

                JSONParser jsonParser = new JSONParser();
                org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(action);

                System.out.println("jsobObcext"+ jsonObject);
                event_name = (String) jsonObject.get("eventName");
                event_time = (String) jsonObject.get("eventStartTime");
                event_location = (String) jsonObject.get("eventLocation");
                String eventCoordinates = (String) jsonObject.get("eventCoordinates");
                String eventEndTime = (String) jsonObject.get("eventEndTime");
                System.out.println("Full Details from Event: Event name:" + event_name + ",EventStartTime:" + event_time + ",EventLocation:" + event_location +  ",EventCoordinates:"+eventCoordinates+  ",EventEndTime:" +eventEndTime );

                TextView tv1 = (TextView)findViewById(R.id.textView4);
                tv1.setText(event_name);
                TextView tv2 = (TextView)findViewById(R.id.textView5);
                tv2.setText(event_time);
                TextView tv3 = (TextView)findViewById(R.id.textView7);
                tv3.setText(event_location);
                WebServerHelper.eventExtras = null;
            } else {
                JSONObject json = null;
                try {
                    String jsonData = "{\"data\":{\"event id\":\"1\",\"event type\":\"class notification\",\"event name\":\"CS8803: Advanced Software Engineering\",\"event time\":\"3:05 PM - MW\",\"event location\":\"Van Leer C457\"}}";
                    json = new JSONObject(jsonData);
                    JSONObject data = json.getJSONObject("data");
                    event_name = data.getString("event name");
                    event_time = data.getString("event time");
                    event_location = data.getString("event location");
                    TextView tv1 = (TextView)findViewById(R.id.textView4);
                    tv1.setText(event_name);
                    TextView tv2 = (TextView)findViewById(R.id.textView5);
                    tv2.setText(event_time);
                    TextView tv3 = (TextView)findViewById(R.id.textView7);
                    tv3.setText(event_location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }




/*
        int notificationId = 001;
        // Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        //viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true);


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GTNOW ALERT")
                        .setContentText("CS8803 is going to start in 30 minutes")
                        .setContentIntent(viewPendingIntent)
                        .extend(wearableExtender);

// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.

        notificationManager.notify(notificationId, notificationBuilder.build());
        */
        getRoute = (Button) findViewById(R.id.route);
        getRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openMap = new Intent("edu.gatech.seclass.GTNow.MapActivity");
                // add json response as extra input parameter
                openMap.putExtra("jsonResponse", WebServerHelper.jsonResponse);
                startActivity(openMap);
            }

        });

        LocationManager mlocManager = null;
        LocationListener mloclistener;

        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mloclistener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                location.getLatitude();
                location.getLongitude();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

         //The latitude and longitude of the current position will be posted to the GTNow server once the setup
        //is completed by the other teams and the required API is made available to the mobile team

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
