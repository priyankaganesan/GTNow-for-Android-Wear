package edu.gatech.seclass.GTNow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

public class MainActivity extends Activity
       implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    //Tag used on log messages.
    static final String TAG = "GTNow";

    TextView textViewLatitude, textViewLongitude;
    Button Login;
    static final int uniqueID = 1348;


    String SENDER_ID = "1085949217955";
    GoogleCloudMessaging gcm;
    String regId;
    SharedPreferences prefs;
    Context context;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(getApplicationContext());

            if (regId.isEmpty()) {
                registerInBackground();
            }
            System.out.println(regId);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
            WebServerHelper.applicationContext = this.getApplicationContext();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        final WearCommunicator wearCommunicator = new WearCommunicator(this);
        Login = (Button) findViewById(R.id.login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText loginText = (EditText)findViewById(R.id.login_text);
                String username = loginText.getText().toString();
                System.out.println("USERNAME = " + username);
                ((GTNowApplication)getApplication()).setGtid(username);
                Intent intent = new Intent("edu.gatech.seclass.GTNow.GTNowMenuActivity");
                startActivity(intent);

//                wearCommunicator.sendMessage("/start-activity", "");

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        if(checkPlayServices())
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    /****** GCM Client Code taken from Google Cloud Messaging demo ******/

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        /*//TODO - App versioning?
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }*/
        System.out.println("regid: " + registrationId);
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                WebServerHelper.sendLocation(mGoogleApiClient);
                WebServerHelper.getGroups("1");
                WebServerHelper.getGroupsList("gburdell");
                return null;
            }
        }.execute(null,null,null);
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    /**
     * Registers the application with GCM servers asynchronously.
     *
     * Stores the registration ID in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            String msg = "";
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    //Sends the registration ID to the server over HTTP
                    //Should be authenticated if using accounts
                    sendRegistrationIdToBackend();

                    msg = "Device Registered + " + regId;

                    //Store regId for future use
                    storeRegistrationId(context, regId);

                   System.out.println(msg);

                } catch (IOException ex) {
                    //TODO
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return "";
            }

            //@Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
                System.out.println(msg);
                Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * to send messages to your app.
     */
    private void sendRegistrationIdToBackend() {
        System.out.println("Sending registration ID: " + regId);
        String username = ((GTNowApplication)getApplication()).getGtid();
        String url = WebServerHelper.SERVER_URL + WebServerHelper.REGISTER_PATH + regId;
        WebServerHelper.postHttp(url, null);
    }

    /**
     * Stores the registration ID in the application's
     * {@code SharedPreferences}.
     *
     */

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        Log.i(TAG, "Saving regId.");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.commit();
    }

    /*private void sendLocation() {
        System.out.println("sending location");
        //if (mGoogleApiClient.blockingConnect().isSuccess()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            String url = WebServerHelper.SERVER_URL + WebServerHelper.SEND_LOCATION_PATH
                    + location.getLatitude() + "/" + location.getLongitude();
        HttpResponse response =  WebServerHelper.postHttp(url, null);
        WebServerHelper.getJsonResponse(response);
        //}
    }*/

}
