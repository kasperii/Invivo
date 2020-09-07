package com.orpheusdroid.screenrecorder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Created by dyoung on 12/13/13.
 */

// This class is based on the example file from Android Beacon library

public class BeaconRecorderApplication extends Application implements BootstrapNotifier, DataInformationChangeListener{
    private static final String TAG = ".MyApplicationName";
    private static int requestCode;
    private static int resultCode;
    private static boolean launchingNotificationIntent;
    private RegionBootstrap regionBootstrap;
    BeaconManager beaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    private String cumulativeLog;
    private RangingService MonitoringService;
    private RangeNotifier myRangeNotifier;
    private BeaconTrackerFragment BeaconTrackerFragment;
    private boolean BeaconRecordingActivated = true;
    public static boolean isRecording;
    SharedPreferences statePrefs;
    SharedPreferences videoPrefs;
    private static Context staticContext;
    public Long lastSeen;
    private long lastShouldRecord;

    private static MediaProjection mMediaProjection;
    private static MediaProjectionManager mProjectionManager;

    public ArrayList<TrackedBeacon> trackedBeacons;
    public ArrayList<TrackedBeacon> foundBeacons;
    public ArrayList<TrackedArea> trackedAreas;

    public boolean monitoring = true;
    private long timeNow;
    private long dataCollectionSessionStartTime;

    static Intent screenshotPermission = null;
    private ArrayList<ProximityData> currentBeaconProximityDataList;
    private boolean searchingForNewBeacons = false;
    private BroadcastReceiver chargerReceiver;
    private MediaRecorder mMediaRecorder;
    private boolean isScreenOn;

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        staticContext = this.getApplicationContext();
        screenshotPermission = null;
        //Get shared preferences for registering state change
        statePrefs = getSharedPreferences("Beacon", Context.MODE_PRIVATE);
        videoPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = staticContext.registerReceiver(null, ifilter);

        currentBeaconProximityDataList = new ArrayList<ProximityData>();

        trackedAreas = getAreaListFromShared("trackedAreas");
        trackedBeacons = getBeaconListFromShared("trackedBeacons");
        if(trackedBeacons==null) {
            trackedBeacons = new ArrayList<TrackedBeacon>();
        }
        foundBeacons = new ArrayList<TrackedBeacon>();
        if(trackedAreas==null){
            trackedAreas = new ArrayList<TrackedArea>();
            createHardCodedBedroomArea();
        }else{
            for(TrackedArea area: trackedAreas){
                for(TrackedBeacon beacon: trackedBeacons){
                    //Double threshold = area.getThresholdDistance(beacon);
                    //beacon.addAreaThresholdData(area.name,threshold);
                    area.updateBeaconReference(beacon);
                }
            }
        }

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
        lastSeen = new Date().getTime();




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
        beaconManager.setBackgroundScanPeriod(1100);

        // Here foreground service ends !
        // all between start and here for only background scanning

//        Notification.Builder debugBuilder = new Notification.Builder(this);
//        debugBuilder.setSmallIcon(R.drawable.ic_notification);
//        debugBuilder.setContentTitle("Update about Beacons");
//        Intent debugIntent = new Intent(this, BeaconTrackerFragment.class);
//        PendingIntent debugPendingIntent = PendingIntent.getActivity(
//                this, 0, debugIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        );
//        debugBuilder.setContentIntent(pendingIntent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("My debug notification Channel ID",
//                    "My debug notification Name", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("My debug Notification Channel Description");
//            NotificationManager notificationManager = (NotificationManager) getSystemService(
//                    Context.NOTIFICATION_SERVICE);
//            notificationManager.createNotificationChannel(channel);
//            builder.setChannelId(channel.getId());
//        }





        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen - DISABLED before activation
        monitoring = true;
        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        
        
        //Set up charger receiver
        Intent uploaderIntent = new Intent(this, UploaderService.class);
        uploaderIntent.setAction(Const.FILE_UPLOADING_START);
        startUploadService(this);
        
        chargerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Awesome things
                Log.d(Const.TAG, "In Recorder service, power is connected ");
                String action = intent.getAction();
                //makeToastHere("Action power");
                Log.d(Const.TAG, "********** " + action);
                if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    //makeToastHere("ACTION_POWER_CONNECTED");
                    Log.d(Const.TAG, "Power connected ");
                    Intent uploaderIntent = new Intent(context, UploaderService.class);
                    uploaderIntent.setAction(Const.FILE_UPLOADING_START);
                    startUploadService(context);
                } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    Log.d(Const.TAG, "Power is not connected ");
                    Intent uploaderStopIntent = new Intent(context, UploaderService.class);
                    uploaderStopIntent.setAction(Const.FILE_UPLOADING_STOP);
                    context.startService(uploaderStopIntent);
                }

            }
        };

        registerReceiver(chargerReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        registerReceiver(chargerReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));

        screenshotPermission = null;
        acquireScreenshotPermission();
        isScreenOn = isScreenOn();
    }



    /// Functions for getting values from the application - class. Possibly bad practice as these
    // variables could be accessed with a direct link to the application

    static boolean getIsRecording(){
        return isRecording;
    }
    void putIsRecording(boolean r){
        isRecording = r;
    }
    void setSearchingForNewBeacons(boolean shouldSearch){
        searchingForNewBeacons = shouldSearch;
    }
    MediaProjectionManager getmProjectionManager(){
        if(mProjectionManager == null){
            mProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        }
        return mProjectionManager;}
    void makeToastHere(String text){
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }
    boolean getMonitoring() {return monitoring;}
    public void setBeaconTrackerFragment(BeaconTrackerFragment fragment) {
        this.BeaconTrackerFragment = fragment;
    }

    // DISABLE or ENABLE MONITORING of beacons
    // great functions to close down full bluetooth functionality with .
    public void disableMonitoring() {
        //makeToastHere("disable monitoring");
        if (regionBootstrap != null) {
            monitoring = false;
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }
    void enableMonitoring() {
        //makeToastHere("enable monitoring");
        monitoring = true;
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    // FUNCTIONS THAT ARE CALLED WHEN BLUETOOTH is found

    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen


        makeToastHere("Enter region");
        //Log.d(TAG, "auto launching MainActivity");

        // Important:  make sure to add android:launchMode="singleInstance" in the manifest
        // to keep multiple copies of this activity from getting created if the user has
        // already manually launched the app.
        //Intent intent = new Intent(this, RangingService.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //this.startService(intent);

        }

    @Override
    public void didExitRegion(Region region) {
        makeToastHere("Exit region");
    }

    // same as above, but abstract, Enter and Exit are only called once
    //https://stackoverflow.com/questions/22689970/android-radiusnetwork-ibeaconlibrary-diddeterminestateforregion-when-is-called
    @Override public void didDetermineStateForRegion(int state, Region region) {
        makeToastHere("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
        if(state == 1){
            //if enter, then create start saving
            startSavingProximityData();
            startRanging();
        }
        else {
            updateBeaconView();
            try {
                resetSavingProximityData(false);
                stopRecordingCall();
                beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            } catch (RemoteException e) {   }
        }

    }


    //This method is called when a beacon that should be tracked is seen!
    private void startRanging(){

        // RangeNotifier runs didRangeBeaconsInRegion whenever beacons are found,
        // ish once a second (update time can be tweaked)
        myRangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.d(TAG, "didRangeBeaconsInRegion: " + beaconManager.getRangingNotifiers());
                //Log.d(TAG,"didRangeBeaconsInRegion");
                timeNow = new Date().getTime();
                Log.d(TAG, "didRangeBeaconsInRegion: time is " + timeNow);
                //for every 15 minutes of continuous ranging, save the data.

                if(timeNow-dataCollectionSessionStartTime > 900000){
                //if(timeNow-dataCollectionSessionStartTime > 60000){
                    Log.d(TAG, "15 minute proximity data storage session reset");
                    resetSavingProximityData(true);
                }
                for (Beacon beacon: beacons) {
                    //Log.d(TAG,"beacon" + beacon.toString());
                    boolean found = false;
                    if(!trackedBeacons.isEmpty()) {
                        //identify if this beacon that is found is tracked
                        for (TrackedBeacon b : trackedBeacons) {
                            if (b.equalId(beacon.getIdentifiers())) {
                                b.updateBeaconObject(beacon);
                                b.addProximity(beacon.getRssi(),timeNow);
                                found = true;
                                if(isBeaconInRecordingRange(b)){
                                    lastShouldRecord = timeNow;
                                    //shouldIRestartRecord(true);
                                }

                                break;
                            }
                        }

                    }
                    //if searching for new beacons
                    // only active when the search window is up
                    if(!found && searchingForNewBeacons){
                        boolean newFound = true;
                        for (TrackedBeacon b : foundBeacons) {
                            if (b.equalId(beacon.getIdentifiers())) {
                                newFound = false;
                                break;
                            }
                        }
                        if(newFound) {
                            TrackedBeacon newTrackedBeacon = new TrackedBeacon(beacon);
                            foundBeacons.add(newTrackedBeacon);
                            updateSearchBeaconView();

                        }
                    }
                }

                //if any of the beacons above returns that it is in recording range,
                // then shouldIRecord returns positive
                if(shouldIRestartRecord()){
                    //makeToastHere("inside threshold: " + trackedBeacons.get(0).getProximity());
                    if(BeaconRecordingActivated && isScreenOn) {
                        if(!isRecording) {
                            startRecordingResetCall();
                        }
                    }
                }
                else{
                    if(isRecording) {
                        stopRecordingCall();
                    }
                }

                updateBeaconView();
            }


        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(myRangeNotifier);
        } catch (RemoteException e) {   }
    }




    // UPDATE VIEW
    // this calls the updateBeaconView if there is an instance of the fragment
    void updateBeaconView(){
        if(this.BeaconTrackerFragment != null) {
            this.BeaconTrackerFragment.updateBeaconView();
        }
    }
    void updateSearchBeaconView(){
        if(BeaconTrackerFragment!=null){
            this.BeaconTrackerFragment.updateSearchBeaconView();
        }
    }






    // RECORD?
    // this methods uses an heuristic evaluation to see if the recorder should be called
    public boolean isBeaconInRecordingRange(TrackedBeacon foundBeacon){
        for (TrackedArea area : trackedAreas) {
            //the distance is measured in negative values, hence the beacon is closer if average distance is bigger than the threshold
            if (foundBeacon.average > area.getThresholdDistance(foundBeacon) && area.isRecordingActive) {
                return true;
            }
        }
        return false;

    }

    // once called the screen recorder is sent and intent to SKIP recording
    public void stopRecordingCall(){
        if(isRecording){
            Intent startIntent = new Intent(this, RecorderService.class);
            startIntent.setAction(Const.SCREEN_RECORDING_SKIP);
            this.startService(startIntent);
        }
        lastSeen = new Date().getTime();
    }
    // once called the screen recorder is sent and intent to SKIP recording
    public void stopRecordingCallFromScreen(){
        isScreenOn = false;
        stopRecordingCall();
    }
    public void screenOn(){
        isScreenOn = true;
    }
    public boolean isScreenOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : dm.getDisplays()) {
            if (display.getState() == Display.STATE_ON) {
                isScreenOn = true;
                return true;
            }
        }
        return false;
    } else {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //noinspection deprecation
        isScreenOn = pm.isScreenOn();
        return isScreenOn;
    }
}

    // once called the screen recorder is sent and intent to START recording
    public void startRecordingResetCall(){
        Log.d(Const.TAG, "Recording Service is running: " + String.valueOf(isServiceRunning(RecorderService.class)));
        if (mMediaProjection == null && !(isRecording)) { //&& !isServiceRunning(RecorderService.class)
            if (mProjectionManager==null){
                mProjectionManager=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
            }
          //Request Screen recording permission
            Log.d(Const.TAG, "send getScreenshotPermission");
            acquireScreenshotPermission();
        } else if (isRecording) {
          Log.d(Const.TAG, "Recording Service is running - skip reset from beacontracker");
          return;
        }
        if(hasScreenshotPermission()){

            Intent recorderService = new Intent(this, RecorderService.class);
            recorderService.setAction(Const.SCREEN_RECORDING_RESTART);
            //It does not use these, as it gets them from myApp in RecorderService now
            recorderService.putExtra(Const.RECORDER_INTENT_DATA, (Intent) screenshotPermission.clone());
            recorderService.putExtra(Const.RECORDER_INTENT_RESULT, resultCode);
            startService(recorderService);

            //Start ScreenReceiver, which makes the the recorder works all the time in screen on
            Intent screenServiceIntent = new Intent(this, ScreenService.class);
            startService(screenServiceIntent);



//           Intent startIntent = new Intent(this, RecorderService.class);
//            if(lastSeen - new Date().getTime()>60000){
//                startIntent.setAction(Const.SCREEN_RECORDING_RESTART);
//            }
//            startIntent.setAction(Const.SCREEN_RECORDING_RESTART);
//            this.startService(startIntent);
        }
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
        createNewArea("Bedroom");
    }
    public void createNewArea(String name){
        TrackedArea newTrackedArea = new TrackedArea(name);
        trackedAreas.add(newTrackedArea);
        updateBeaconView();
    }

    public ArrayList<TrackedBeacon> getTrackedBeacons(){
        return trackedBeacons;
    }
    public ArrayList<TrackedBeacon> getFoundBeacons(){
        return foundBeacons;
    }
    public ArrayList<TrackedArea> getTrackedAreas(){
        return trackedAreas;
    }



    // SAVE and RETURN object from internal storage
    public void saveObjectInShared(ArrayList myListOfObjects, String s){

        SharedPreferences.Editor myEdit = statePrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myListOfObjects);
        myEdit.putString(s, json);
        myEdit.commit();
    }
    public ArrayList<TrackedBeacon> getBeaconListFromShared(String s){
        Gson gson = new Gson();
        String json = statePrefs.getString(s, "");
        ArrayList obj = gson.fromJson(json, new TypeToken<ArrayList<TrackedBeacon>>(){}.getType());//ArrayList.class)
        return obj;
    }
    public ArrayList<TrackedArea> getAreaListFromShared(String s){
        Gson gson = new Gson();
        String json = statePrefs.getString(s, "");
        ArrayList obj = gson.fromJson(json, new TypeToken<ArrayList<TrackedArea>>(){}.getType());
        return obj;
    }
    public void syncBeaconAreaToShared(){
        saveObjectInShared(trackedAreas,"trackedAreas");
        saveObjectInShared(trackedBeacons,"trackedBeacons");
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /// SCREENSHOT PERMISSION HANDLER
    //the following functions are implemented to open and create a new notification
    //that opens a  ScreenshotPermissionRequest. Else the app cannot start from background.

    public static void acquireScreenshotPermission() {
        try {
            if (hasScreenshotPermission() && !launchingNotificationIntent) {
//                if(null != mMediaProjection) {
//                    mMediaProjection.stop();
//                    mMediaProjection = null;
//                }
//                //should this be here?
//                mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
            } else {
                openScreenshotPermissionRequester();
            }
        } catch (final RuntimeException ignored) {
            openScreenshotPermissionRequester();
        }
    }
    protected static void openScreenshotPermissionRequester(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // This is at least android 10...
            if(!launchingNotificationIntent){
                final Intent intent = new Intent(staticContext, AcquireScreenshotPermissionIntent.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //staticContext.startActivity(intent);
                launchNotificationIntent(intent);
            }
        }else{
            final Intent intent = new Intent(staticContext, AcquireScreenshotPermissionIntent.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            staticContext.startActivity(intent);
        }
    }
    protected void setScreenshotPermission(final int req, final int resc, final Intent permissionIntent) {
        requestCode = req;
        resultCode = resc;
        screenshotPermission = permissionIntent;
        launchingNotificationIntent = false;
    }
    public static Intent getScreenshotPermission() {
        return (Intent) screenshotPermission.clone();
    }

    public static boolean hasScreenshotPermission(){
        Log.d(TAG, "hasScreenshotPermission: " + screenshotPermission);
        Log.d(TAG, "hasScreenshotPermission: " + (screenshotPermission!=null));
        return screenshotPermission!=null;
    }
    public MediaProjection getmMediaProjection(){
        try {
            if(null != mMediaProjection) {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
            mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) screenshotPermission.clone());
            return mMediaProjection;

        } catch (final RuntimeException ignored) {
            openScreenshotPermissionRequester();
            return null;
        }
    }


    private static void launchNotificationIntent(Intent i) {
        launchingNotificationIntent = true;
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // This is at least android 10...
            NotificationManager mgr = (NotificationManager) staticContext.getSystemService(Context.NOTIFICATION_SERVICE);
            //if (mgr.getNotificationChannel(CHANNEL_WHATEVER) == null) {
                mgr.createNotificationChannel(new NotificationChannel("my call notification intent channel",
                        "Whatever", NotificationManager.IMPORTANCE_HIGH));
            //}

            mgr.notify(111, buildNormal(staticContext, i).build());

        }
    }
    private static NotificationCompat.Builder buildNormal(Context context, Intent intent) {
        NotificationCompat.Builder b=
                new NotificationCompat.Builder(context, "my call notification intent channel");
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Accept recording permission"))
                .setContentText("Accept permission to activate the screen recording application")
                .setFullScreenIntent(buildPendingIntent(context, intent), true);

        return(b);

    }

    //This sets should record - but also provides a 5 second delay until not valid
    private boolean shouldIRestartRecord(){
        long timeNow = new Date().getTime();
        //if(!(timeNow-lastShouldRecord<5000)){makeToastHere("too long I since saw a beacon, shutdown!");}
        return timeNow-lastShouldRecord<5000;
    }

    public boolean shouldIRecordAtScreenReceived(){
        if(shouldIRestartRecord()) {
            return true;
        }
//        for (TrackedBeacon b : trackedBeacons) {
//            if(isBeaconInRecordingRange(b)){
//                if(shouldIRestartRecord()) {
//                    return true;
//                }
//
//            }
//        }
        return false;
    }

    private static PendingIntent buildPendingIntent(Context context, Intent intent) {

    return(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
}


//Method that initiates the beacon proximity object that is stored.
    private void startSavingProximityData(){
        long startTime = System.currentTimeMillis();
        dataCollectionSessionStartTime = startTime;
        if(!currentBeaconProximityDataList.isEmpty()){Log.d(TAG,"start new saving though last data list is not empty");}

        for(TrackedBeacon trackedBeacon: trackedBeacons) {
            currentBeaconProximityDataList.add( new ProximityData(trackedBeacon.getUuid(),startTime, getProximitySaveName(trackedBeacon),trackedBeacon.getProximityData(),trackedBeacon.getAreaThresholdData(),getAndroidID()));
        }
    }
    private String getAndroidID() {

        return Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private void resetSavingProximityData(boolean shouldRestart){
        Log.d(TAG, "resetSavingProximityData: RESET!");
        long endTime = System.currentTimeMillis();
        // TODO: create
        for(ProximityData proxData: currentBeaconProximityDataList) {
            proxData.setTimeEnd(endTime);
            proxData.setNewList();
        }
        saveProximityData();
        if(shouldRestart){ startSavingProximityData();}
    }

    //saves and removes all proxmity data
    private void saveProximityData(){
        //Log.d(TAG, "start writing prox data!");
        try {
            for (ProximityData proxData : currentBeaconProximityDataList) {
                Log.d(TAG, "prox data is this long: " + proxData.getProximities().size());
                if(proxData.getOldProximities().size() != 0) {
                    JsonUtil jsonUtil = new JsonUtil();
                    String jsonString = jsonUtil.toJson(proxData);
                    String fileName = proxData.getFileName();
                    jsonUtil.writeJsonFile(jsonString, fileName);
                    Log.d(TAG, "saveProximityData: filename:" + fileName);
                    proxData.resetProximities();
                    syncBeaconAreaToShared();
                }
            }
            currentBeaconProximityDataList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private String getFileSaveName(TrackedBeacon b) {
        String filename = videoPrefs.getString(getString(R.string.filename_key), "yyyyMMdd_hhmmss");
        String prefix = videoPrefs.getString(getString(R.string.fileprefix_key), "recording");
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(filename);
        return prefix + "_" + formatter.format(today) + "_" + b.getDescription();
    }
    private String getProximitySaveName(TrackedBeacon b) {
        String filename = videoPrefs.getString(getString(R.string.filename_key), "yyyyMMdd_hhmmss");
        String prefix = "proximity";
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(filename);
        return prefix + "_" + formatter.format(today) + "_" + b.getDescription() + "_beacon";
    }

    public boolean isNoOtherAreaBySameName(String editText) {
        String name = editText.toString();
        for(TrackedArea area: trackedAreas){
            if(name.equalsIgnoreCase(area.getName())){
                return false;
            }
        }
        return true;
    }

    public void removeArea(TrackedArea area){
        trackedAreas.remove(area);
    }

    @Override
    public void resetDataCollection() {
        resetSavingProximityData(true);
    }

    public static void startUploadService(Context context) {
        if (((NetworkUtil.isWifiConnected(context) || NetworkUtil.isConnected(context)) && (NetworkUtil.getConnectivityStatus(context
        )== Const.TYPE_WIFI || (NetworkUtil.getConnectivityStatus(context)== Const.TYPE_MOBILE)))) { //&& Power.isCharging(context)
            // it uploads on wifi and mobile network

            Log.d(Const.TAG, "Start uploading service...");
            Log.d(Const.TAG, String.valueOf(NetworkUtil.getConnectivityStatus(context)));
            Intent uploaderIntent = new Intent(context, UploaderService.class);
            uploaderIntent.setAction(Const.FILE_UPLOADING_START);
            context.startService(uploaderIntent);
        } else {
            Log.d(Const.TAG, "NO POWER OR NO WIFI...");
            Intent uploaderStopIntent = new Intent(context, UploaderService.class);
            uploaderStopIntent.setAction(Const.FILE_UPLOADING_STOP);
            context.startService(uploaderStopIntent);
        }
    }


}