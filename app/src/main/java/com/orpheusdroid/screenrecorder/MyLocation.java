package com.orpheusdroid.screenrecorder;

import java.sql.Time;

public class MyLocation {

    private double Lat;
    private double Altitude;
    private double Long;
    private long timeStamp;
    private float locationAcc;

    public float getLocationAcc() {
        return locationAcc;
    }

    public void setLocationAcc(float locationAcc) {
        this.locationAcc = locationAcc;
    }

    public double getLatitude() {
        return Lat;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }

    public void setLatitude(double latitude) {
        Lat = latitude;
    }

    public double getAltitude() {
        return Altitude;
    }

    public void setAltitude(double altitude) {
        Altitude = altitude;
    }

}
