package com.orpheusdroid.screenrecorder.beaconTracker;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class TrackedArea {
    public HashMap<String, Double> trackedBeaconsInArea;
    public String name;

    public TrackedArea(String myName) {
        name = myName;
        trackedBeaconsInArea = new HashMap<String, Double> ();
    }

    //this method returns the threshold distance registered to the beacon
    public Double getThresholdDistance(TrackedBeacon foundBeacon) {
        return trackedBeaconsInArea.get(foundBeacon.getUuid());
    }

    public void addBeaconThreshold(TrackedBeacon b, Double thresholdDistance){
        trackedBeaconsInArea.put(b.getUuid(), thresholdDistance);
        b.addArea(this);
    }
    public void removeBeaconThreshold(TrackedBeacon b){
        trackedBeaconsInArea.remove(b);
        b.removeArea(this);
    }
    public void calibrateBeaconThreshold(TrackedBeacon b, Double thresholdDistance){
        trackedBeaconsInArea.put(b.getUuid(), thresholdDistance);
    }
}
