package com.orpheusdroid.screenrecorder;

class AreaThresholds {
    private double thresholdProximity;
    private String areaName;

    AreaThresholds(String areaName, double tp){
        this.thresholdProximity = tp;
        this.areaName = areaName;
    }

    double getThresholdProximity() {
        return thresholdProximity;
    }

    void setThresholdProximity(double thresholdProximity) {
        this.thresholdProximity = thresholdProximity;
    }

    String getAreaName() {
        return areaName;
    }

    void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
