package com.orpheusdroid.screenrecorder;

import java.sql.Time;

public class App {
    private String appName;
    private long timeStamp;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeSamp) {
        this.timeStamp = timeSamp;
    }
}
