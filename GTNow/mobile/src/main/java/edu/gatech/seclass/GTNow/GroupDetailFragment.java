package edu.gatech.seclass.GTNow;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A fragment representing a single Group detail screen.
 * This fragment is either contained in a {@link edu.gatech.seclass.GTNow.GroupListActivity}
 * in two-pane mode (on tablets) or a {@link edu.gatech.seclass.GTNow.GroupDetailActivity}
 * on handsets.
 */
public class GroupDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private String groupJson = null;
    private Group group = null;
    View rootView = null;
    private String groupId = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            groupId = getArguments().getString(ARG_ITEM_ID);
            //groupJson = GroupActivity.getGroup(groupId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_group_detail, container, false);
        displayGroup();
        return rootView;
    }

    public void displayGroup() {
        if (groupId == null) return;
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                parseGroupJson(GroupActivity.getGroup(groupId));
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (group == null || rootView == null) return;
                ((TextView) rootView.findViewById(R.id.group_name)).setText(group.name);
                ((TextView) rootView.findViewById(R.id.start_date)).setText(group.startDate);
                ((TextView) rootView.findViewById(R.id.end_date)).setText(group.endDate);
                ((ListView) rootView.findViewById(R.id.members_list))
                        .setAdapter(new ArrayAdapter<String>(
                                getActivity(),
                                android.R.layout.simple_list_item_1,
                                group.members));
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (groupId == null) return;
                        Intent intent = new Intent("edu.gatech.seclass.GTNow.GroupMapActivity");
                        intent.putExtra(ARG_ITEM_ID, groupId);
                        startActivity(intent);
                    }
                };

                ((Button) rootView.findViewById(R.id.findGroupBtn)).setOnClickListener(listener);
            }
        }.execute(null,null,null);
    }

    private void parseGroupJson(String jsonStr) {
        if (jsonStr == null) return;
        try {
            JSONObject json = new JSONObject(jsonStr);
            String name = json.has("name") ? json.getString("name") : "";
            String startDate = json.has("createTime") ? json.getString("createTime") : "";
            String endDate = json.has("expireTime") ? json.getString("expireTime") : "";
            ArrayList<String> members = GroupActivity.getGroupMembers(groupId);
            //ArrayList<String> members = new ArrayList<String>();
            /*if (json.has("members")) {
                JSONArray memArr = json.getJSONArray("members");
                for (int i = 0, len = memArr.length(); i < len; ++i) {
                    members.add(memArr.getString(i));
                }
            }*/
            group = new Group(name, startDate, endDate, members);
        }catch(JSONException e){
            System.err.println("Error parsing JSON group detail string");
            System.err.println(e.toString());
        }
    }

    private static class Group {
        public final String name, startDate, endDate;
        public final ArrayList<String> members;

        Group(String name, String startDate, String endDate, ArrayList<String> members) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.members = members;
        }
    }
}
