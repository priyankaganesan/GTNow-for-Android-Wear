package edu.gatech.seclass.GTNow;

import java.util.HashMap;

/**
 * Created by ramiksadana on 11/29/14.
 */
public class DataHolder {
    private HashMap<String, String> data;
    public HashMap<String, String> getData() {return data;}
    public void setData(HashMap<String, String> data) {this.data = data;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}