package com.orpheusdroid.screenrecorder.beaconTracker;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.orpheusdroid.screenrecorder.App;
import com.orpheusdroid.screenrecorder.Const;
import com.orpheusdroid.screenrecorder.MyNotification;
import com.orpheusdroid.screenrecorder.R;
import com.orpheusdroid.screenrecorder.RecorderService;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dyoung on 12/13/13.
 */

// This class is based on the example file from Android Beacon library

public class BeaconRecorderApplication extends Application implements BootstrapNotifier {
    private static final String TAG = ".MyApplicationName";
    private RegionBootstrap regionBootstrap;
    BeaconManager beaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    private String cumulativeLog;
    private RangingService MonitoringService;
    private RangeNotifier myRangeNotifier;
    private BeaconTrackerFragment BeaconTrackerFragment;
    private boolean BeaconRecordingActivated = false;
    private boolean isRecording = true;
    SharedPreferences statePrefs;

    public ArrayList<TrackedBeacon> trackedBeacons;
    public ArrayList<TrackedArea> trackedAreas;

    public boolean monitoring = true;



    // these class fields are used for the rolling average calculation
    double average = 1;
    Queue<Double> lastTenDistances = new LinkedList<>(Arrays.asList(average,average,average,average,average,average,average,average,average,average));

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        //Get shared preferences for registering state change
        statePrefs = getSharedPreferences("State", Context.MODE_PRIVATE);

        //TODO: create trackedBeacons; = new ArrayList<trackedBeacons>(); if not in statePrefs
        trackedBeacons = new ArrayList<TrackedBeacon>();
        trackedAreas = new ArrayList<TrackedArea>();
        createHardCodedBedroomArea();

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(" m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(false);

        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(1000l);





        // Here foreground service starts !
        // The code below allows for use of foreground service to scan for beacons. This unlocks
        // the ability to continually scan for long periods of time in the background on Andorid 8+
        // in exchange for showing an icon at the top of the screen and a always-on notification to
        // communicate to users that your app is using resources in the background.


        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, BeaconTrackerFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(100);

        // Here foreground service ends !
        // all between start and here for only background scanning






        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen - DISABLED before activation
        monitoring = true;
        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }


    public void disableMonitoring() {
        //makeToastHere("disable monitoring");
        if (regionBootstrap != null) {
            monitoring = false;
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }
    public void enableMonitoring() {
        makeToastHere("enable monitoring");
        monitoring = true;
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    public void makeToastHere(String text){
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }




    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen

        Log.d(TAG, "did enter region.");

        Log.d(TAG, "auto launching MainActivity");

        Intent intent = new Intent(this, RangingService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Important:  make sure to add android:launchMode="singleInstance" in the manifest
        // to keep multiple copies of this activity from getting created if the user has
        // already manually launched the app.
        this.startService(intent);

        }



    @Override
    public void didExitRegion(Region region) {
        makeToastHere("exit region");
        //MyNotification.createNotification(this, createStringOut("didExitRegion"), "testString");
    }

    //https://stackoverflow.com/questions/22689970/android-radiusnetwork-ibeaconlibrary-diddeterminestateforregion-when-is-called
    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        makeToastHere("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
        //logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
        //change this to show in notification or UI
        if(state == 1){
            startRanging();
        }
        else {
            updateBeaconView();
            //TODO: stop recording here!
            try {
                stopRecordingCall();
                isRecording = false;
                beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            } catch (RemoteException e) {   }
        }

    }


    //This method is called when a beacon that should be tracked is seen!
    private void startRanging(){

        //TODO: Create JSON?


        myRangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                for (Beacon beacon: beacons) {

                    if(!trackedBeacons.isEmpty()) {
                        boolean found = false;
                        //identify if this beacon that is found is tracked
                        for (TrackedBeacon b : trackedBeacons) {
                            if (b.equalId(beacon.getIdentifiers())) {
                                b.updateBeaconObject(beacon);
                                Log.d(TAG, "same uuid: " + b.getUuid());
                                found = true;

                                if(shouldIRecord(b)){
                                    //makeToastHere("inside 5 meters ");
                                    Log.d(TAG, "I see a beacon that is less than 5 meters away.");
                                    //TODO: send data back to application?
                                    if(BeaconRecordingActivated) {
                                        if(!isRecording) {
                                            isRecording = true;
                                            startRecordingResetCall();
                                        }
                                    }
                                }else{
                                    stopRecordingCall();
                                    isRecording = false;
                                }

                                break;
                            }
                        }
                        if(!found){
                            TrackedBeacon newTrackedBeacon = new TrackedBeacon(beacon);
                            trackedBeacons.add(newTrackedBeacon);
                        }

                    }else{
                                TrackedBeacon newTrackedBeacon = new TrackedBeacon(beacon);
                                trackedBeacons.add(newTrackedBeacon);
                    }

                }

                updateBeaconView();
                if (beacons.size() > 0) {
                  //Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                    Beacon firstBeacon = beacons.iterator().next();
                    Log.d(TAG, "The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                    //makeToastHere("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                  //TODO: eval if recording should restart.


                  }
                  //TODO: Send to JSON? should this be done every second?

                  //Look at JsonUtil - and also recorder service public void stopScreenRec() {


                  //logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                  //MyNotification.createNotification(getApplicationContext(), createRangingStringOut(firstBeacon.getDistance()), "testString");
            }


        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(myRangeNotifier);
        } catch (RemoteException e) {   }
    }




    //this calls the updateBeaconView if there is an instance of the fragment
    public void updateBeaconView(){
        if(this.BeaconTrackerFragment != null) {
            this.BeaconTrackerFragment.updateBeaconView();
        }
    }







    // this methods uses an heuristic evaluation to see if the recorder should be called
    public boolean shouldIRecord(TrackedBeacon foundBeacon){

        Double dist = foundBeacon.getProximity();
        //the distance is added to a queue, where only the last 10 are, by removing and adding
        //it calculates the average distance.

        if(lastTenDistances.size() < 10){
            // Now initialised with 10 values, this is not used anymore
            lastTenDistances.add(dist);
            average = (lastTenDistances.size()*average+dist)/(lastTenDistances.size()+1);
            return false;
        } else{
            lastTenDistances.add(dist);
            double removedDigit = (double) lastTenDistances.poll();
            average = (10*average-removedDigit+dist)/10;
        }
        Log.d(TAG,"average: "+average);
        for(TrackedArea area: trackedAreas){
            if (average < area.getThresholdDistance(foundBeacon)){
                return true;
            }
        }
        return false;
        //TODO: Implement that this looks at how close it has been on average, and how close the threshold areas are
    }

    //TODO: Add send Enter Broadcast method
    public void sendEnterBroadcast(){
//        Intent intent = new Intent();
//        intent.setAction(Const.ACTION_ENTER_BEACON);
//        sendBroadcast(intent);
        //TODO: SEND UPDATE TO PREFERENCE AND ACTIVATE state change service instead (with logic)
        //TODO: should I add debug - or save value option so I can see that the state is changed!

    }
    public void sendExitBroadcast(){
//        Intent intent = new Intent();
//        intent.setAction(Const.ACTION_EXIT_BEACON);
//        sendBroadcast(intent);

    }
    //TODO: Add send Exit Broadcast method

    // once called the screen recorder is sent and intent to SKIP recording
    public void stopRecordingCall(){
        Intent startIntent = new Intent(this, RecorderService.class);
        startIntent.setAction("com.orpheusdroid.screenrecorder.services.action.skiprecording");
        this.startService(startIntent);
    }

    // once called the screen recorder is sent and intent to START recording
    public void startRecordingResetCall(){
        sendEnterBroadcast();
//
//        Intent startIntent = new Intent(this, RecorderService.class);
//        startIntent.setAction("com.orpheusdroid.screenrecorder.services.action.restartrecording");
//        this.startService(startIntent);
    }

    // this method allows the beaconTrackerFragment, once instanced, to send the context to this application
    public void setBeaconTrackerFragment(BeaconTrackerFragment fragment) {
        this.BeaconTrackerFragment = fragment;
    }


    // logToDisplay is used for debug reasons, if there is an instance of the fragments,
    // then the log is updated in the fragments updateLog method.


    //TODO: this function will read all "tracked beacon" objects and add them to the recycle view
    private void loadTrackedObjects(){
        //statePrefs
        //1. look for tracked beacon object list in shared pref
        //2. look for tracked locations
    }

    private void changeTrackedObjects(){
        //1. store the tracked beacon object list in shared pref
        //2. store the look for tracked locations
    }

    private void createHardCodedBedroomArea(){
        TrackedArea newTrackedArea = new TrackedArea("Bedroom");
        trackedAreas.add(newTrackedArea);
    }

    public ArrayList<TrackedBeacon> getTrackedBeacons(){
        return trackedBeacons;
    }
    public ArrayList<TrackedArea> getTrackedAreas(){
        return trackedAreas;
    }


    public boolean getMonitoring() {return monitoring;}


}