package com.orpheusdroid.screenrecorder;

import java.net.URL;
import java.sql.Time;
import java.util.List;
import java.util.TimeZone;

public class Video {

    private String userID;
    private String videoPath;
    private URL uploadURL;
    private long startTime; //start time of the video
    private long endTime; // end time of the video
    public TimeZone timeZone;
    private List<MyLocation> locationList;
    private List<App> appList;
    private boolean isLocationGranted;

    public void setLocationGranted(boolean locationGranted) {
        isLocationGranted = locationGranted;
    }

    public boolean isLocationGranted() {
        return isLocationGranted;
    }


    public Video() {
        this.timeZone = TimeZone.getDefault();
    }


    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getVideoPath() {

        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public long getStartTime() {

        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<MyLocation> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<MyLocation> locationList) {
        this.locationList = locationList;
    }

    public List<App> getAppList() {
        return appList;
    }

    public void setAppList(List<App> appList) {
        this.appList = appList;
    }
}
