package edu.gatech.seclass.GTNow;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class GroupActivity extends Activity {

    private static final String GROUP_COUNT = "/group-count";
    private static final long CONNECTION_TIME_OUT_MS = 100 ;
    private static HashMap<String, String> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        groups = DataHolder.getInstance().getData();
        if(groups == null) {
            groups = new HashMap<String, String>(2);
            groups.put("group_id_0", "1");
            groups.put("group_name_0", "Group1");
            groups.put("group_id_1", "2");
            groups.put("group_name_1", "Group2");
        }

        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new GridPagerAdapter(getApplicationContext(), getFragmentManager()));
    }


    public class GridPagerAdapter extends FragmentGridPagerAdapter {

        private Context mContext;
        private ArrayList<GridRow> mRows;
        private PhoneCommunicator phoneCommunicator;

        public GridPagerAdapter(Context mContext, FragmentManager fm) {
            super(fm);
            this.mContext = mContext;
            phoneCommunicator = new PhoneCommunicator(mContext);
            initAdapter();
        }
        /**
         * This method is used for demonstration only. In a real app the data and the adapters would
         * probably come from somewhere else.
         */
        private void initAdapter() {
            mRows = new ArrayList<GridRow>();
            if(groups != null) {
                for (int i = 0; i < groups.size() / 2; i++) {
                    GridRow tempRow = new GridRow();
                    tempRow.addPage(new GridPage(groups.get("group_id_"+i), groups.get("group_name_" + i)));
                    mRows.add(tempRow);
                }
            }
        }

        @Override
        public Fragment getFragment(int row, int col) {
            if(col == 0) {
                GridPage page = mRows.get(row).getPage(col);
                CardFragment cardFragment = CardFragment.create(page.getTitle(), page.getText(), page.getIcon());

                return cardFragment;
            } else {
                ButtonFragment fragment = ButtonFragment.newInstance(groups.get("group_id_"+row), phoneCommunicator);
                fragment.setCardGravity(Gravity.BOTTOM);
                return fragment;
            }
        }

//        @Override
//        public ImageReference getBackground(int row, int column) {
//            GridPage page = mRows.get(row).getPage(column);
//            return ImageReference.forDrawable(page.getBackground());
//        }

        @Override
        public int getRowCount() {
            if(groups != null) {
                return groups.size() / 2;
            } else
                return 0;
        }

        @Override
        public int getColumnCount(int row) {
            return 2;
        }
    }

    public class GridRow  {
        ArrayList<GridPage> mPages = new ArrayList<GridPage>();
        public GridRow() {}
        public GridRow(ArrayList<GridPage> mPages) {
            this.mPages = mPages;
        }
        public GridPage getPage(int index) {
            return mPages.get(index);
        }
        public void addPage(GridPage mPage) {
            mPages.add(mPage);
        }
        public int getSize() {
            return mPages.size();
        }
        public ArrayList<GridPage> getPagesArray() {
            return mPages;
        }
        public void setPages(ArrayList<GridPage> mPages) {
            this.mPages = mPages;
        }
    }

    public class GridPage  {
        private String mTitle;
        private String mText;
        private int mIcon;
        private int mBackground;
        /**
         * Constructor for the GridPage
         * @param mTitle
         *          Title for the card
         * @param mText
         *          Text for the card
         * @param mIcon
         *          Icon that will be on the right of the title
         * @param mBackground
         *          The Background image to be used by the fragment. The card will overlay on top of the background.
         */
        public GridPage(String mTitle, String mText, int mIcon, int mBackground) {
            this.mTitle = mTitle;
            this.mText = mText;
            this.mIcon = mIcon;
            this.mBackground = mBackground;
        }

        public GridPage(String mTitle, String mText) {
            this.mTitle = mTitle;
            this.mText = mText;
        }

        public String getTitle() {
            return mTitle;
        }
        public String getText() {
            return mText;
        }
        public int getIcon() {
            return mIcon;
        }
        public int getBackground() {
            return mBackground;
        }
    }

    public static class ButtonFragment extends CardFragment {

        private String id;
        private PhoneCommunicator phoneCommunicator;

        public ButtonFragment() {

        }

        public ButtonFragment(String id, PhoneCommunicator phoneCommunicator) {
            this.id = id;

            this.phoneCommunicator = phoneCommunicator;
        }

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

                            phoneCommunicator.sendFindFriends(id);
                        }
                    }).start();
                }
            });
            return button;
        }

        public static ButtonFragment newInstance(String id, PhoneCommunicator phoneCommunicator) {
            return new ButtonFragment(id, phoneCommunicator);
        }
    }
}
