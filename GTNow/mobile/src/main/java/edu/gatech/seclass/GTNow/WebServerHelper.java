package edu.gatech.seclass.GTNow;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

public final class WebServerHelper {

    public static final String SERVER_URL = "https://crafty-chiller-763.appspot.com/";
    public static final String REGISTER_PATH = "_ah/api/registration/v1/registerDevice/";
    public static final String SEND_LOCATION_PATH = "_ah/api/integration/v1/currentLocation/";
    public static final String GET_GROUPS_PATH = "_ah/api/group/v1/mybean/";
    public static final String GET_GROUPS_LIST_PATH = "_ah/api/group/v1/listGroups/";
    public static Bundle eventExtras = null;
    public static String jsonResponse = null;
    public static Context applicationContext=null;
    public static PolylineOptions lineOptions = new PolylineOptions();
    public static Location location;
    public static double latitude;
    public static double longitude;


    protected static HttpResponse postHttp(String url, List<NameValuePair> pairs) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        System.out.println("Sending HTTP Post: " + url);
        try {
            if (pairs != null)
                httpPost.setEntity(new UrlEncodedFormEntity(pairs));
            HttpResponse response = httpClient.execute(httpPost);
            return response;
        } catch (IOException e) {
            System.out.println("IOException: " + e.toString());
        }
        return null;
    }

    protected static void sendLocation(GoogleApiClient mGoogleApiClient) {
        System.out.println("sending location");
        //Client must already be connected
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // By default, the location is set to Van Leer "latitude":"33.7757","longitude":"-84.3974
        latitude = 33.7757;
        longitude = -84.3974;
        if(location != null)  {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        String url = SERVER_URL + SEND_LOCATION_PATH
                + latitude + "/" + longitude;
        HttpResponse response = postHttp(url, null);
        getJsonResponse(response);
    }

    public static String getGroupsList(String userId) {
        System.out.println("Getting groups List");


        String url = WebServerHelper.SERVER_URL + WebServerHelper.GET_GROUPS_LIST_PATH
                + userId;
        HttpResponse response =  WebServerHelper.getHttp(url, null);

        String reponseFromServer = "";
        reponseFromServer = GetGroupsListJson(response);
        return reponseFromServer;
    }

    private static String GetGroupsListJson(HttpResponse response) {
        String op = "";
        try {

            JSONParser jsonParser = new JSONParser();

            if (response != null) {

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity()
                                .getContent()));

                String strLine = "";
                while ((strLine = rd.readLine()) != null) {
                    op += strLine;
                }

                System.out.println("List of Groups: " + op);


            };
        }
        catch (IOException ex) {
            System.out.println("Group Exception:" + ex.toString());
        }
        return op;
    }

    public static ArrayList<GroupDetailsObject> getGroups(String groupId) {
        System.out.println("Getting groups");
        String url = WebServerHelper.SERVER_URL + WebServerHelper.GET_GROUPS_PATH
                + groupId;
        HttpResponse response =  WebServerHelper.getHttp(url, null);

        ArrayList<GroupDetailsObject> groupDetails=  GetGroupsJson(response);
        return groupDetails;
    }

    static boolean firstTime = true;
    private static ArrayList<GroupDetailsObject> GetGroupsJson(HttpResponse response) {

        ArrayList<GroupDetailsObject> groupCoordinates= new ArrayList<GroupDetailsObject>();
        try {
            String op = "";
            JSONParser jsonParser = new JSONParser();

            if (response != null) {

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity()
                                .getContent()));

                String strLine = "";
                while ((strLine = rd.readLine()) != null) {
                    op += strLine;
                }

                System.out.println("Group members: " + op);
                System.out.println("Group members: " + op);
                JSONObject jsonObject = (JSONObject) jsonParser.parse(op);


                if (jsonObject.containsKey("data")) {
                    String jsonString1 = (String) jsonObject.get("data");


                    System.out.println("Response from Group" + jsonString1);
                    JSONObject jsonDataObject = (JSONObject) jsonParser.parse(jsonString1);
                    JSONArray jsonArrayItems = (JSONArray) jsonDataObject.get("items");
                    System.out.println("Items: " + jsonArrayItems.toString());
                    for (int i = 0; i < jsonArrayItems.size(); i++) {
                        JSONObject jsonItemsObject = (JSONObject) jsonArrayItems.get(i);
                        Double latitude = (Double) jsonItemsObject.get("locLatitude");
                        Double longitude = (Double) jsonItemsObject.get("locLongitude");
                        String gtid = (String) jsonItemsObject.get("gtid");
                        groupCoordinates.add(new GroupDetailsObject(latitude, longitude, gtid));
                    }
                }
            }

            if(firstTime) {
                firstTime = false;
            } else {

                WearListenerService.onMessageLocate(groupCoordinates);
            }
        }
        catch (IOException ex) {
            System.out.println("Group Exception:" + ex.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return groupCoordinates;
    }

    protected static HttpResponse getHttp(String url, List<NameValuePair> pairs) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        System.out.println("Sending group HTTP Get: " + url);
        try {
            HttpResponse response = httpClient.execute(request);
            return response;
        } catch (IOException e) {
            System.out.println("IOException: " + e.toString());
        }
        return null;
    }


    public static void getJsonResponse(HttpResponse response) {
        // parse the response and store in jsonResponse which will be used by Map activity.
        JSONParser jsonParser = new JSONParser();

        try {
            if(response != null ) {
                /*
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity()
                                .getContent()));
                String op = "";
                String strLine = "";
                while ((strLine = rd.readLine()) != null) {
                    op += strLine;
                }
                */
                /* Read from file
                BufferedReader bufferedReader = new BufferedReader(new FileReader("sdcard/json.txt"));

                StringBuilder builder = new StringBuilder();
                String line = null;

                while((line =bufferedReader.readLine())!=null){

                    builder.append(line);
                }

                */
                // byte buffer into a string
                lineOptions.getPoints().clear();
                InputStream ips = response.getEntity().getContent();
                BufferedReader buf = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
                StringBuilder builder = new StringBuilder();
                String s;
                while (true) {
                    s = buf.readLine();
                    if (s == null || s.length() == 0)
                        break;
                    builder.append(s);
                }
                buf.close();
                ips.close();


                jsonResponse = builder.toString();

                jsonResponse.replace("\\\"","");

                JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonResponse);

                String jsonString1 = (String)jsonObject.get("data");

                JSONObject jsonDataObject = (JSONObject) jsonParser.parse(jsonString1);
                JSONArray jsonArrayRoutes= (JSONArray) jsonDataObject.get("routes");

                JSONObject jsonRoutesObject = (JSONObject)jsonArrayRoutes.get(0);
                JSONArray jsonArrayLegs= (JSONArray) jsonRoutesObject.get("legs");
                JSONObject jsonLegsObject = (JSONObject)jsonArrayLegs.get(0);
                JSONArray jsonArraySteps= (JSONArray) jsonLegsObject.get("steps");
                for(int i=0; i<jsonArraySteps.size(); i++) {
                    JSONObject jsonStepsObject = (JSONObject)jsonArraySteps.get(i);

                    JSONObject jsonStartlocation = (JSONObject)jsonStepsObject.get("start_location");
                    //Log.d("TAG",start_location.toString());
                    Double latitude = (Double)jsonStartlocation.get("lat");
                    Double longitude = (Double)jsonStartlocation.get("lng");
                    lineOptions.add(new LatLng(latitude,longitude));

                    System.out.println("Latitude: " + latitude);
                }

                /*
                JSONObject jsonData = new JSONObject(jsonResponse);

                JSONArray jsonRoutes = jsonData.getJSONArray("routes");
                JSONObject jsonObj = jsonRoutes.getJSONObject(0);
                JSONArray jsonLegs = jsonObj.getJSONArray("legs");
                JSONObject jsonObj1 = jsonLegs.getJSONObject(0);
                JSONArray jsonSteps = jsonObj1.getJSONArray("steps");

                for(int i = 0; i < jsonSteps.length(); i++){
                    JSONObject c = jsonSteps.getJSONObject(i);
                    String latitude = c.getJSONObject("start_location").getString("lat");
                    String longitude = c.getJSONObject("start_location").getString("lng");
                    System.out.println("RESPONSE OUTPUT: " + latitude);
                    lineOptions.add(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));

                }

                // Store the response in a WebserverHelper member variable.
                */
            }
        }catch (IOException e) {
            System.out.println("IOException: " + e.toString());
        }
            catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
