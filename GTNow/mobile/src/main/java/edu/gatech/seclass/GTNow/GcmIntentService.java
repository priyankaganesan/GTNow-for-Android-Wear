package edu.gatech.seclass.GTNow;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/****** GCM Client Code taken from Google Cloud Messaging demo ******/

public class GcmIntentService extends IntentService {


    public static Context applicationContext=null;
    public static final int REMINDER_ID = 1; //notification id for event reminders
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    GoogleApiClient mGoogleApiClient;
    WearCommunicator wearCommunicator;
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wearCommunicator = new WearCommunicator(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Retrieve data sent from server
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        System.out.println("handling gcm intent");
        System.out.println("outside extras"+ extras.isEmpty());
        if (!extras.isEmpty()) {
            System.out.println("Extras from GCM");
            Toast.makeText(getApplicationContext(), "Msg Queue Not Empty", Toast.LENGTH_SHORT);
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String action = extras.getString("action", "default");
                System.out.println("Action from GCM" + action);
                try {
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(action);
                    System.out.println("Event Json Object" + jsonObject);
                    String eventType = (String) jsonObject.get("action");
                    System.out.println("Event Type:" + eventType);
                    if (eventType.equals("get_location")) {
                        if (mGoogleApiClient.blockingConnect().isSuccess()) {
                            WebServerHelper.sendLocation(mGoogleApiClient);
                        }
                    } else if (eventType.equals("reminder")) {
                        System.out.println("Inside Event Type:" + eventType);
                        sendEventNotification(jsonObject, extras);
                    }
                }
                catch(Exception ex) {
                    System.out.println("Exception in parsing" + ex.toString());
                }

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendEventNotification(JSONObject jsonObject, Bundle extras) {
        int notificationId = 001;
        // Build intent for notification content
        Intent viewIntent = new Intent(this, EventActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        String eventName = (String) jsonObject.get("eventName");
        String eventStartTime = (String) jsonObject.get("eventStartTime");
        String eventLocation = (String) jsonObject.get("eventLocation");
        String eventCoordinates = (String) jsonObject.get("eventCoordinates");
        String eventEndTime = (String) jsonObject.get("eventEndTime");
        System.out.println("Full Details: Event name:" + eventName + ",EventStartTime:" + eventStartTime + ",EventLocation:" + eventLocation +  ",EventCoordinates:"+eventCoordinates+  ",EventEndTime:" +eventEndTime );


        viewIntent.putExtra("eventLocation", eventLocation);
        viewIntent.putExtra("eventCoordinates", eventCoordinates);
        viewIntent.putExtra("eventEndTime", eventEndTime);
        viewIntent.putExtra("eventName", eventName);
        viewIntent.putExtra("eventStartTime", eventStartTime);

        WebServerHelper.eventExtras=extras;
        // Create a WearableExtender to add functionality for wearables
        //        NotificationCompat.WearableExtender wearableExtender =
        //                new NotificationCompat.WearableExtender()
        //                        .setHintHideIcon(true);


        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GTNOW ALERT")
                        .setContentText(eventName + " is going to start at " + eventStartTime)
                        .setContentIntent(viewPendingIntent)
                        .setExtras(extras)
                        .setLocalOnly(true);
//                        .extend(wearableExtender);
        ;

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Build the notification and issues it with notification manager.

        notificationManager.notify(notificationId, notificationBuilder.build());


        //Priyanka: Sending message to wear on notification

        wearCommunicator.sendNotification(eventName + " is going to start at " + eventStartTime);
        Log.i("GTNow", "sending notification to wear");
    }


}