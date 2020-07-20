package com.orpheusdroid.screenrecorder;

import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TrackedArea {

    public LinkedHashMap<String, Double> trackedBeaconThresholdInArea;
    public LinkedHashMap<String, TrackedBeacon> trackedBeaconsObjectsInArea;
    public Drawable out;
    public Drawable see;
    public Drawable in;
    public boolean isRecordingActive = true;

    public String name;

    public TrackedArea(String myName) {
        name = myName;
        trackedBeaconThresholdInArea = new LinkedHashMap<String, Double> ();
        trackedBeaconsObjectsInArea = new LinkedHashMap<String, TrackedBeacon> ();
    }

    public String getName(){return this.name; }

    public void setName(String n){this.name = n;}

    //this method returns the threshold distance registered to the beacon
    public Double getThresholdDistance(TrackedBeacon foundBeacon) {
        if (trackedBeaconThresholdInArea.get(foundBeacon.getUuid()) == null){
            double nullDist = -1;
            return nullDist;
        }
        return trackedBeaconThresholdInArea.get(foundBeacon.getUuid());
    }

    public void addBeaconThreshold(TrackedBeacon b, Double thresholdDistance){

        trackedBeaconThresholdInArea.put(b.getUuid(), thresholdDistance);
        trackedBeaconsObjectsInArea.put(b.getUuid(), b);
        //b.addArea(this);
    }
    public void removeBeaconThreshold(TrackedBeacon b){
        trackedBeaconThresholdInArea.remove(b);
        trackedBeaconsObjectsInArea.remove(b);
        //b.removeArea(this);
    }

    public void removeBeaconAllThreshold(){

    }

    public void updateBeaconReference(TrackedBeacon b){
        trackedBeaconsObjectsInArea.put(b.getUuid(), b);
    }
    public void calibrateBeaconThreshold(TrackedBeacon b, Double thresholdDistance){
        trackedBeaconThresholdInArea.put(b.getUuid(), thresholdDistance);
    }
    public HashMap<String, Double> getHash(){
        return trackedBeaconThresholdInArea;
    }

    //getHashByIndex returns the String for each of the beacons based on the list order
    // this is sorted such that the first three are the visible, and latest added.
    public TrackedBeacon getObjectByIndex(int i) {
        if(trackedBeaconThresholdInArea.size()-1<i){
            return null;
        }
        Set<Map.Entry<String, Double>> mapSet = trackedBeaconThresholdInArea.entrySet();
        Map.Entry<String, Integer> beaconAtI = (Map.Entry<String, Integer>) mapSet.toArray()[i];
        if (!(beaconAtI == null)) {
            return trackedBeaconsObjectsInArea.get(beaconAtI.getKey());
        }
        return null;

    }

    //sets proximity to current prox of the beacon at position i
    public Double setProximityByIndex(int i) {
        if(trackedBeaconThresholdInArea.size()-1<i){
            return 0.00;
        }
        Set<Map.Entry<String, Double>> mapSet = trackedBeaconThresholdInArea.entrySet();
        Map.Entry<String, Integer> beaconAtI = (Map.Entry<String, Integer>) mapSet.toArray()[i];
        if (!(beaconAtI == null)) {
            Double prox = trackedBeaconsObjectsInArea.get(beaconAtI.getKey()).getProximity(); //getRunningAverageRssi()

            trackedBeaconThresholdInArea.put(beaconAtI.getKey(), prox);
            trackedBeaconsObjectsInArea.get(beaconAtI.getKey()).addAreaThresholdData(this.name,prox);
            return prox;
        }
        return -1.00;

    }

    public void setActivated(boolean activated) {
        isRecordingActive = activated;
    }
}
