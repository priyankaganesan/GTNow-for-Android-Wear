package edu.gatech.seclass.GTNow;


import android.app.Application;
import android.content.Context;

public class GTNowApplication extends Application {
    private static final String GTID_DEFAULT = "gburdell";
    private String gtid = GTID_DEFAULT;
    private static Context context;
    //public GoogleApiClient mGoogleApiClient = null;

    protected String getGtid() {
        return gtid;
    }

    protected void setGtid(String gtid) {
        if (gtid == null || gtid.isEmpty()) gtid = GTID_DEFAULT;
        this.gtid = gtid;
    }

    public void onCreate(){
        super.onCreate();
        GTNowApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GTNowApplication.context;
    }
}
