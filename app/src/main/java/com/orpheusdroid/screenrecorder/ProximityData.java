package com.orpheusdroid.screenrecorder;

import java.util.ArrayList;
import java.util.TimeZone;

public class ProximityData {
    private String beaconUuid;
    private ArrayList<ProximityDataPoint> proximities;
    private ArrayList<AreaThresholds> areaThresholds;
    private long timeStart;
    private long timeEnd;
    private String fileName;
    private String userID;
    public TimeZone timeZone;
    // One area point ID + One area point threshold proximity (for each beacon)
    // If threshold proximity changes - create new ProximityData.
    // If lost connection, create new file and save old, each 15 minutes, create file?
    // 5 proximities * each time

    ProximityData(String bu, long start, String fn, ArrayList<ProximityDataPoint> pr,ArrayList<AreaThresholds> at, String uid){
        timeStart = start;
        beaconUuid = bu;
        fileName = fn;
        proximities = pr;
        areaThresholds = at;
        userID = uid;
        this.timeZone = TimeZone.getDefault();
    }

    ArrayList<ProximityDataPoint> getProximities() {
        return proximities;
    }

    public void addProximities(ArrayList<ProximityDataPoint> proximities) {
        this.proximities = proximities;
    }

    public void resetProximities(){
        this.proximities.clear();
    }

    public String getUserID() {
        return userID;
    }

    String getBeaconUuid() {
        return beaconUuid;
    }




    public void setBeaconUuid(String beaconUuid) {
        this.beaconUuid = beaconUuid;
    }

    public ArrayList<AreaThresholds> getAreaThresholds() {
        return areaThresholds;
    }

    public void setAreaThresholds(ArrayList<AreaThresholds> areaThresholds) {
        this.areaThresholds = areaThresholds;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public String getFileName() {
        return fileName;
    }
}
