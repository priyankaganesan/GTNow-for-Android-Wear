package edu.gatech.seclass.GTNow;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ramiksadana on 11/24/14.
 */
public class WearCommunicator {
    GoogleApiClient mGoogleApiClient;
    private static final long CONNECTION_TIME_OUT_MS = 100 ;
    private String nodeId;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DIRECTIONS_PATH = "/directions";
    private static final String NOTIFICATION = "/notification";
    private static final String MAP = "/map";

    public WearCommunicator(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        retrieveNodeId();
    }

    private void retrieveNodeId() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                }
                mGoogleApiClient.disconnect();


            }
        }).start();
    }

    public void sendToast() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, START_ACTIVITY_PATH, null);
                    mGoogleApiClient.disconnect();
                }
            }).start();
        }
    }

    //Call this method to send message to wear.
    public void sendMessage( final String path, final String text ) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, path, text.getBytes());
                    mGoogleApiClient.disconnect();

                }
            }).start();
        }
        Log.d("TAG", "Message sent");
    }

    public void sendDirections( final String text ) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, DIRECTIONS_PATH, text.getBytes());
                    mGoogleApiClient.disconnect();
                }
            }).start();
        }
    }

    public void sendMap(final Bitmap bitmap) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Asset asset = createAssetFromBitmap(bitmap);
                    PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
                    DataMap map = dataMap.getDataMap();
                    map.putAsset("mapImage", asset);
                    map.putLong("time", new Date().getTime());
                    PutDataRequest request = dataMap.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                            .putDataItem(mGoogleApiClient, request);
                    mGoogleApiClient.disconnect();
                    Log.i("GTNow", "Sent Image");
                }
            }).start();
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public void sendNotification( final String contentText ) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("GTN", "Inside send notification");
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Log.i("GTN", "Inside send notification1");
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, NOTIFICATION, contentText.getBytes());
                    Log.i("GTN", "Inside send notification2");
                    mGoogleApiClient.disconnect();
                }
            }).start();
        } else {
            Log.i("GTN", "node null2");
        }
    }

    public void sendGroupLocations(final String groupId) {
        //Henry, parse the following json https://crafty-chiller-763.appspot.com/_ah/api/group/v1/mybean/1 (The mobile team will call this method with this json)
        //Extract all locations. Call google static maps with the locations. Modify the WearStaticMapService
        //Send the static map over to the wear
        //David's part: Static map is received in ListenerService
        //Triggers new activity that contains a single image showing the map.
        try {
//            JSONObject jsonObj = new JSONObject(json);
//            JSONArray jsonData = jsonObj.getJSONArray("data");
//            JSONArray jsonLegs = jsonRoutes.getJSONObject(0).getJSONArray("legs");
//            jsonSteps = jsonLegs.getJSONObject(0).getJSONArray("steps");
//            for(int index = 0; index < jsonSteps.length(); index++){
//                JSONObject c = jsonSteps.getJSONObject(index);
//                String distance = c.getJSONObject(DISTANCE).getString("text");
//                String duration = c.getJSONObject(DURATION).getString("text");
//                String direction = c.getString(INSTRUCTIONS);
//                direction = direction.replaceAll("<[^>]*>", "");
//                distances.add(distance);
//                durations.add(duration);
//                directions.add(direction);
            ArrayList<GroupDetailsObject> group_mbrs = WebServerHelper.getGroups(groupId); // uses mobile team thing to do it. method needs group id.
            WearStaticMapService.onMessageLocate(group_mbrs);
        }
        catch (Exception ex){
            Log.i("Exception", ex.getMessage());
        }
    }
}
