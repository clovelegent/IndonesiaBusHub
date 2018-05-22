package com.clove.indonesiabushub.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.clove.indonesiabushub.BusStation;
import com.clove.indonesiabushub.dataprovider.BusLineContentProvider;
import com.clove.indonesiabushub.server.LocationService;
import com.clove.indonesiabushub.settings.BusSettingsFragment;

import java.util.ArrayList;

public class Utils {
    private static final String TAG = "Utils";

    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 通过经纬度获取距离(单位：米)
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }

    public static boolean getDistance2(double lat1, double lng1, double lat2, double lng2) {
        double latGap = Math.abs(lat1 - lat2);
        double lngGap = Math.abs(lng1 -lng2);
        Log.d(TAG,"latGap = "+latGap+",lngGap = "+lngGap);
        return ((latGap < 0.00001) && (lngGap <0.00001));
    }

    public static void updateCurrentBusLineStation(Context context,ContentResolver mContentResolver){
        String line = getLine(context);
        Log.d(TAG,"select line :"+line);
        if (!line.equals("")){
            if (LocationService.allBusStation == null){
                LocationService.allBusStation = new ArrayList<>();
            }
            LocationService.allBusStation.clear();
            Cursor mCursor = mContentResolver.query(BusLineContentProvider.LINES_CONTENT_URI, null, "line=?", new String[]{line}, null);
            while(mCursor.moveToNext()){
                String mLine = mCursor.getString(mCursor.getColumnIndex("line"));
                String mName = mCursor.getString(mCursor.getColumnIndex("name"));
                double mlatitude = mCursor.getDouble(mCursor.getColumnIndex("latitude"));
                double mLongitude = mCursor.getDouble(mCursor.getColumnIndex("longitude"));
                BusStation busStation = new BusStation(mLine,mName,mlatitude,mLongitude);
                Log.d(TAG,busStation.toString());
                LocationService.allBusStation.add(busStation);
            }
        }
    }

    @NonNull
    public static String getLine(Context context) {
        return context.getSharedPreferences(BusSettingsFragment.DEF_SP_NAME, Context.MODE_PRIVATE).getString(BusSettingsFragment.KEY_LINES_CHOOSE_VALUE,"");
    }

    public static String getStationName(String allName){
        int start = allName.indexOf(":");
        return allName.substring(start+1);
    }

}
