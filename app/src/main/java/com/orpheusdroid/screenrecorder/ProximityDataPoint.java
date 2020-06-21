package com.orpheusdroid.screenrecorder;

import java.sql.Timestamp;

public class ProximityDataPoint {
    private double proximityPoint;
    private long timestamp;

    ProximityDataPoint(double p, long t){
        this.proximityPoint = p;
        this.timestamp = t;

    }

    public double getProximityPoint() {
        return proximityPoint;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setProximityPoint(double proximityPoint) {
        this.proximityPoint = proximityPoint;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
