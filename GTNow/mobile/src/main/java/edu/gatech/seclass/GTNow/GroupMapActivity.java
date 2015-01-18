package edu.gatech.seclass.GTNow;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class GroupMapActivity extends FragmentActivity implements LocationListener {

    GoogleMap mGoogleMap;
    double mLatitude=0;
    double mLongitude=0;
    int flag;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // code to get jsonRespose added as a extra input parameter to the intent
        Bundle bundle = getIntent().getExtras();
        groupId = bundle.getString(GroupDetailFragment.ARG_ITEM_ID);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if(status!= ConnectionResult.SUCCESS){

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else {
            GoogleMap mGoogleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mGoogleMap == null) System.out.println("MAP NULL!!!!!");
            else System.out.println("Map not null");

         //   mGoogleMap.setMyLocationEnabled(true);

            //-84.4049986,\"lat\":

            //LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location;
            LocationManager mlocManager = null;
            LocationListener mloclistener;

            mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            mloclistener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    flag = 1;

                    location.getLatitude();
                    location.getLongitude();
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    System.out.println("I am here"  + mLatitude);

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
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, mloclistener );
//            location = mlocManager.getLastKnownLocation("gps");
//            mLatitude = location.getLatitude();
//            mLongitude = location.getLongitude();
            System.out.println("I am outside"  + mLatitude);
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Location location1 = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
           // double lat = location1.getLatitude();
            //double long1 = location1.getLongitude();

            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.7757, -84.3974)).title("I am here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            updateGroupCoordinates(mGoogleMap);
           /* for(int i=0; i< groupCoordinates.size();i++) {
                System.out.println("\ngroupCoordinates: Lat: " + groupCoordinates.get(i).latitude + " Long: " +  groupCoordinates.get(i).longitude );
                   // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.5778, -84.496603)).title("Friend 1"));
                   // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.7875769, -84.4067726)).title("Friend 2"));
                   // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.7678, -84.4049986)).title("Friend 3"));
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(groupCoordinates.get(i).latitude,groupCoordinates.get(i).longitude)).title(groupCoordinates.get(i).gtid));

            }
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(WebServerHelper.latitude, WebServerHelper.longitude ), 10.0f) );

            String data = "";
            String jsonData= "{\"data\":{\"LatLng\":[\"33.7836, -84.4004\",\"33.7836, -84.4006\",\"33.7827, -84.4006\",\"33.7827, -84.4006\",\"33.7827, -84.4004\",\"33.7826, -84.4003\",\"33.7825,-84.4003\"]}}";//
            //        LatLng origin = new LatLng(33.7836, -84.4004);
            //        LatLng p1 = new LatLng(33.7836, -84.4006);
            //        LatLng p3 = new LatLng(33.7827, -84.4006);
            //        LatLng p4 = new LatLng(33.7827, -84.4006);
            //        LatLng p5 = new LatLng(33.7827, -84.4004);
            //        LatLng p6 = new LatLng(33.7826, -84.4003);
            //        LatLng dest = new LatLng(33.7825,-84.4003);*/

            //JSONObject json = null;
            /*
            json = new JSONObject(jsonData);
            JSONObject data1 = json.getJSONObject("data");
            JSONArray LatLng = data1.getJSONArray("LatLng");
            //PolylineOptions lineOptions = new PolylineOptions();
            for(int i=0; i<LatLng.length();i++)
            {
                String pos= LatLng.getString(i);
                String latlng[] = pos.split(",");
                double latitude = Double.parseDouble(latlng[0]);
                double longitude = Double.parseDouble(latlng[1]);
                lineOptions.add(new LatLng(latitude,longitude));
            }
            //        lineOptions.add(origin,p1,p3,p4,p5,p6,dest);
            System.out.println("LineOptions" + lineOptions.getPoints());
               */



        }


    }

    private void updateGroupCoordinates(final GoogleMap map) {
        new AsyncTask<Void, Void, ArrayList<GroupDetailsObject>>() {
            @Override
            protected ArrayList<GroupDetailsObject> doInBackground(Void... params) {
                return WebServerHelper.getGroups(groupId);
            }
            @Override
            protected void onPostExecute(ArrayList<GroupDetailsObject> list) {
                ArrayList<GroupDetailsObject> groupCoordinates =
                        (list == null) ? new ArrayList<GroupDetailsObject>() : list;
                for(int i=0; i< groupCoordinates.size();i++) {
                    System.out.println("\ngroupCoordinates: Lat: "
                            + groupCoordinates.get(i).latitude + " Long: "
                            + groupCoordinates.get(i).longitude );
                    System.out.println(groupCoordinates.get(i).gtid);
                    // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.5778, -84.496603)).title("Friend 1"));
                    // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.7875769, -84.4067726)).title("Friend 2"));
                    // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(33.7678, -84.4049986)).title("Friend 3"));
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(groupCoordinates.get(i).latitude,groupCoordinates.get(i).longitude))
                            .title(groupCoordinates.get(i).gtid));

                }
                System.out.println("mGoogleMap null?: " + (mGoogleMap == null));
                map.moveCamera( CameraUpdateFactory.newLatLngZoom(
                        new LatLng(WebServerHelper.latitude, WebServerHelper.longitude), 15.0f) );

            }
        }.execute(null, null, null);
    }

    /** A class to download data from Google Directions URL */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}
