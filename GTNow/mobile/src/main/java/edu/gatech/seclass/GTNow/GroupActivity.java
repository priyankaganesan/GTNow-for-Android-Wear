package edu.gatech.seclass.GTNow;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;


public abstract class GroupActivity extends Activity {
    private static final String GROUPS_JSON =
            "\"data\": {{\"items\": [{\"id\": 1, \"name\": \"Group1\"}, " +
                    "{\"id\": 2, \"name\": \"Group2\"}]}}";

    private static final String GROUP_ONE_JSON =
            "{\"name\": \"Group1\", \"createTime\": \"2014-11-09 12:00:00\", " +
            "\"expireTime\":\"2014-12-05 23:59:59\", \"members\":[\"gburdell\",\"user1\",\"user2\"]}";

    private static final String GROUP_TWO_JSON =
            "{\"name\": \"Group2\", \"createTime\": \"2014-11-09 12:00:00\", " +
            "\"expireTime\":\"2014-12-05 23:59:59\", \"members\":[\"gburdell\",\"user3\",\"user4\"]}";

    //Returns JSON string of groups for this user
    //Should be called in AsyncTask because will eventually use
    public static String getGroups(Application app) {
        String username = ((GTNowApplication)app).getGtid();
        String groupsJson = WebServerHelper.getGroupsList(username);
        System.out.println("Groups JSON: " + groupsJson);
        return groupsJson;
    }

    public static String getGroup(String groupId) {
        if ("1".equals(groupId)) {
            return GROUP_ONE_JSON;
        } else if ("2".equals(groupId)) {
            return GROUP_TWO_JSON;
        }
        return "";
    }

    public static ArrayList<String> getGroupMembers(String groupId) {
        ArrayList<String> members = new ArrayList<String>();
        if (groupId == null) return members;
        ArrayList<GroupDetailsObject> details = WebServerHelper.getGroups(groupId);
        if (details == null) return members;
        for (int i = 0, len = details.size(); i < len; ++i) {
            String gtid = details.get(i).gtid;
            if (gtid != null) members.add(gtid);
        }
        return members;
    }
}
