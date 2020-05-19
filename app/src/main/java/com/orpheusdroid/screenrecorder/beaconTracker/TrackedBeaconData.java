package com.orpheusdroid.screenrecorder.beaconTracker;


//TODO: this is just placeholder
public class TrackedBeaconData {

    private String description;
    private String uuid;
    private int minor;
    private int major;
    private int[] room;

    public TrackedBeaconData(String description, String uuid) {
        this.description = description;
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getMinor() {
        return minor;
    }
    public void setMajor(int major) {
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }
    //TODO: this should be implemented properly - where should the beacons be placed
    public void setRoom(int[] rooms) {
        this.room = rooms;
    }
    //TODO: this should also be implemented properly - where should the beacons be placed

    public int[] getRooms() {
        return room;
    }
}