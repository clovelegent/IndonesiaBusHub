package com.clove.indonesiabushub;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.clove.indonesiabushub.utils.Utils;

import java.util.Objects;

public class BusStation {

    private static final String TAG = "BusStation";

    private String line;
    private String name;
    private double latitude;
    private double longitude;
    private boolean isArrived;

    public BusStation(String line, String name, double latitude, double longitude) {
        this.line = line;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isArrived() {
        return isArrived;
    }

    public void setArrived(boolean arrived) {
        isArrived = arrived;
    }

    public boolean arriveStation(double latitude,double longitude){
        double distance = Utils.getDistance(latitude,longitude,this.latitude,this.longitude);
        Log.d(TAG,"distance = "+distance);
        if(distance <= 30){
            return true;
        }
        return false;

        /*boolean isArrive = Utils.getDistance2(latitude,longitude,this.latitude,this.longitude);

        Log.d(TAG,"isArrive:"+isArrive);

        return isArrive;*/
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStation that = (BusStation) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(line, that.line) &&
                Objects.equals(name, that.name);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {

        return Objects.hash(line, name, latitude, longitude);
    }

    @Override
    public String toString() {
        return "BusStation{" +
                "line='" + line + '\'' +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
