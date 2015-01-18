package edu.gatech.seclass.GTNow;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapsActivity extends Activity {

    private TextView mTextView;
    private PhoneCommunicator phoneCommunicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        phoneCommunicator = new PhoneCommunicator(this);
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager(), phoneCommunicator));
    }

    public void triggerDirections(View view) {
        Intent i = new Intent(this, Main.class);
        startActivity(i);
        //phoneCommunicator.sendMessage("directions","Get directions");
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            mTextView.setText(message);
        }
    }

    public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

        private final Context mContext;
        private PhoneCommunicator phoneCommunicator;

        public SampleGridPagerAdapter(Context ctx, FragmentManager fm, PhoneCommunicator phoneCommunicator) {
            super(fm);
            mContext = ctx;
            this.phoneCommunicator = phoneCommunicator;
        }

        final int[] LOGO_IMAGES = new int[]{
                R.drawable.map,
                R.drawable.directions,
        };

        // A simple container for static data in each page
        private class Page {
            // static resources
            int cardGravity = Gravity.BOTTOM;
            boolean expansionEnabled = false;
            float expansionFactor = 1.0f;
            int expansionDirection = CardFragment.EXPAND_DOWN;
        }

        @Override
        public Fragment getFragment(int row, int col) {
            if (row == 0) {
                MapFragment fragment = MapFragment.newInstance(this.phoneCommunicator);
                fragment.setCardGravity(Gravity.BOTTOM);
                return fragment;
            } else {

                DirectionsFragment fragment = DirectionsFragment.newInstance(this.phoneCommunicator);
                fragment.setCardGravity(Gravity.BOTTOM);
                return fragment;
            }
//            else
//            {
//                GroupFragment fragment = GroupFragment.newInstance();
//                fragment.setCardGravity(Gravity.BOTTOM);
//                return fragment;
//            }
        }

        @Override
        public int getRowCount() {
            return 2;
        }

        @Override
        public int getColumnCount(int i) {
            return 1;
        }

        // Create a static set of pages in a 2D array
        private final Page[][] PAGES = {{new Page(), new Page(), new Page()}};
    }

    public static class DirectionsFragment extends CardFragment {

        static PhoneCommunicator phoneCommunicator;

        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            ImageButton button = new ImageButton(this.getActivity());
            button.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(280, 150);
            button.setLayoutParams(params);
            button.setImageDrawable(getResources().getDrawable(R.drawable.directions));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("GTNow_Wear", "Get Directions Clicked");
//                            Intent intent = new Intent(getActivity(), Main.class);
//                            startActivity(intent);
                            phoneCommunicator.sendGetDirections();
                        }
                    }).start();
                }
            });
            return button;
        }

        public static DirectionsFragment newInstance(PhoneCommunicator phoneCommunicator) {
            DirectionsFragment directionsFragment = new DirectionsFragment();
            directionsFragment.phoneCommunicator = phoneCommunicator;
            return directionsFragment;
        }
    }

    public static class MapFragment extends CardFragment {

        static PhoneCommunicator phoneCommunicator;
        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            ImageButton button = new ImageButton(this.getActivity());
            button.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(280, 150);
            button.setLayoutParams(params);
            button.setImageDrawable(getResources().getDrawable(R.drawable.map));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                            phoneCommunicator.sendMessage("start", null);
                            phoneCommunicator.sendMessage("locate", "");
//                            Log.d("MyFragment", "onClick");
                        }
                    }).start();
                }
            });
            return button;
        }

        public static MapFragment newInstance(PhoneCommunicator phoneCommunicator) {
            MapFragment mapFragment = new MapFragment();
            mapFragment.phoneCommunicator = phoneCommunicator;
            return mapFragment;
        }
    }

    public static class GroupFragment extends CardFragment {


        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            ImageButton button = new ImageButton(this.getActivity());
            button.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(280, 150);
            button.setLayoutParams(params);
            button.setImageDrawable(getResources().getDrawable(R.drawable.findfriends));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("MyFragment", "onClick");
                            Intent intent = new Intent(getActivity(), GroupActivity.class);
                            startActivity(intent);
                        }
                    }).start();
                }
            });
            return button;
        }

        public static GroupFragment newInstance() {
            return new GroupFragment();
        }
    }

}
