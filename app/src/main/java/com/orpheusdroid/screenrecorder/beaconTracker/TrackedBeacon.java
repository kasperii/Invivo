package com.orpheusdroid.screenrecorder.beaconTracker;


import androidx.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//TODO: this is just placeholder
public class TrackedBeacon {

    private String description;
    private String uuid;
    private int minor;
    private int major;
    private int[] room;
    private Beacon myBeaconObject;
    //this is an array with pointers to TrackedArea
    public ArrayList<TrackedArea> trackedAreas;

    //this obejct is used in the beacon, it consists of the UUID, minor and major.
    private List<Identifier> mIdentifiers;
    private String id;

    public TrackedBeacon(Beacon beaconObject) {
        myBeaconObject = beaconObject;
        mIdentifiers = myBeaconObject.getIdentifiers();
        this.uuid = mIdentifiers.get(0).toHexString();
    }

    public void updateBeaconObject(Beacon beaconObject) {
        myBeaconObject = beaconObject;
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

    public List<Identifier> getId(){return mIdentifiers;}

    public boolean equalId(@Nullable List<Identifier> otherId) {
        return mIdentifiers.equals(otherId);
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

    public Double getProximity(){return myBeaconObject.getDistance();}

    public void addArea(TrackedArea a){trackedAreas.add(a);}
    public void removeArea(TrackedArea a){trackedAreas.remove(a);}



}