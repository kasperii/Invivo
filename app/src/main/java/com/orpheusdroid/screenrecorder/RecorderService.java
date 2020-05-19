/*
 * Copyright (c) 2016-2017. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.orpheusdroid.screenrecorder;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.location.Location;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.orpheusdroid.screenrecorder.gesture.ShakeEventManager;


import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by vijai on 12-10-2016.
 */
//TODO: Update icons for notifcation
public class RecorderService extends AccessibilityService implements ShakeEventManager.ShakeListener {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static int WIDTH, HEIGHT, FPS, DENSITY_DPI;
    private static int BITRATE;
    private static boolean mustRecAudio;
    private static String SAVEPATH;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private boolean isRecording;
    private boolean stopped;
    private boolean isSkipped;
    private int recordingCounter = 0;
    private boolean useFloatingControls;
    private boolean showTouches;
    private FloatingControlService floatingControlService;
    private boolean isBound = false;
    private NotificationManager mNotificationManager;
    private Video video;
    private App app;
    private String locationTAG = "LocationTest";

    private BroadcastReceiver chargerReceiver;
    public MyLocation userLocation = new MyLocation();
    public List<MyLocation> locationList = new ArrayList<>();
    private List<App> appList = new ArrayList<>();
    private JsonUtil jsonUtil; // Json file
    private String jsonString;
    private String jsonFileName;
    private MyLocation loc;

    private long startTime, elapsedTime = 0;
    private long startVideoTime, endVideoTime, pauseTime = 0;
    private SharedPreferences prefs;
    private WindowManager window;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

        }
    };
    private ShakeEventManager mShakeDetector;
    private Intent data;
    private int result;
    private Intent previousData;
    private int previousResult;
    //Service connection to manage the connection state between this service and the bounded service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Get the service instance
            FloatingControlService.ServiceBinder binder = (FloatingControlService.ServiceBinder) service;
            floatingControlService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            floatingControlService = null;
            isBound = false;
        }
    };


    @Override
    public void onServiceConnected() {
        Log.v(Const.TAG, "***** onServiceConnected");

    }


    @Override
    public void onCreate() {

        jsonUtil = new JsonUtil();
        chargerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Awesome things
                Log.d(Const.TAG, "In Recorder service, power is connected ");
                String action = intent.getAction();

                Log.d(Const.TAG, "********** " + action);
                if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    Log.d(Const.TAG, "Power connected ");
                    Intent uploaderIntent = new Intent(context, UploaderService.class);
                    uploaderIntent.setAction(Const.FILE_UPLOADING_START);
                    startService(context);
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

        getLastLocation();
        startLocationUpdates();

    }


    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(Const.MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(Const.MIN_TIME_BW_UPDATES);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }


    public void onLocationChanged(Location location) {
        // New location has now been determined
        if (location != null) {
            Log.d(locationTAG, "Got location changed");
            Log.d(locationTAG, String.valueOf(location.getLatitude()));
            Log.d(locationTAG, String.valueOf(location.getLatitude()));
            userLocation.setLatitude(location.getLatitude());
            userLocation.setAltitude(location.getAltitude());
            userLocation.setLong(location.getLongitude());
            userLocation.setTimeStamp(System.currentTimeMillis());
            userLocation.setLocationAcc(location.getAccuracy());
            locationList.add(userLocation);
        }
    }


    public void getLastLocation() {
        mFusedLocationClient = getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                          @Override
                                          public void onSuccess(Location location) {
                                              // Got last known location. In some rare situations this can be null.
                                              if (location != null) {
                                                  // Logic to handle location object
                                                  userLocation.setLatitude(location.getLatitude());
                                                  userLocation.setAltitude(location.getAltitude());
                                                  userLocation.setLong(location.getLongitude());
                                                  userLocation.setTimeStamp(System.currentTimeMillis());
                                                  userLocation.setLocationAcc(location.getAccuracy());
                                                  locationList.add(userLocation);

                                                  Log.d(locationTAG, location.toString());
                                                  Log.d(locationTAG, String.valueOf(location.getLatitude()));
                                                  Log.d(locationTAG, String.valueOf(location.getLatitude()));
                                              }
                                          }
                                      }
                )

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public static void startService(Context context) {
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


    private String getAndroidID() {

        return Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //return super.onStartCommand(intent, flags, startId);
        //Find the action to perform from intent
        switch (intent.getAction()) {
            case Const.SCREEN_RECORDING_START:
                startScreenRec(intent);
                break;
//            case Const.SCREEN_RECORDING_PAUSE:
//                if (isRecording) {
//                    pauseTime = System.currentTimeMillis();
//                    //pauseScreenRecording();
//                    stopScreenRec(); // stop the file, the different in logic between stop and pause is handled in the restart
//
//                }
//                break;
//            case Const.SCREEN_RECORDING_RESUME:
//                if (isRecording) {
//                    resumeScreenRecording();
//                }
//                break;

            case Const.SCREEN_RECORDING_SKIP:
                if (isRecording) {
                    pauseTime = System.currentTimeMillis();
                    isSkipped = true;
                    stopScreenRec(); // stop the file, the different in logic between stop and pause is handled in the restart
                }
                break;
            case Const.SCREEN_RECORDING_RESTART:
                // We are recording only in screen on/off, when the screen is off we are not recording (in case we need it change the flag in screen off in ScreenReceiver )
                if (!stopped && !isSkipped) {
                    //get all the users log data for the recorded video
                    stopScreenRec();
                    startScreenRec(intent);
                }
                 else if (isSkipped) {
                    startScreenRec(intent);
                    isSkipped = false;
                }else if (stopped) {
                    break;
                }
                break;
            case Const.SCREEN_RECORDING_STOP:
                //get all the users log data for the recorded video
                isSkipped = false;
                stopScreenRec();
                break;
            case Const.SCREEN_RECORDING_DESTORY_SHAKE_GESTURE:
                mShakeDetector.stop();
                stopSelf();
                break;
        }
        return START_STICKY;
    }

    public void stopScreenRec() {
        endVideoTime = System.currentTimeMillis();
        long recTime = (System.currentTimeMillis() - startVideoTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(recTime);

        Log.d(Const.TAG, "***** Seconds: " + String.valueOf(seconds));

        if (video == null || seconds == 0) {
            Log.d(Const.TAG, "***** Seconds returned: " + String.valueOf(seconds));
            return;
        }

        video.setEndTime(endVideoTime);
        video.setVideoPath(getFileSaveName());
        video.setAppList(appList);
        video.setLocationList(locationList);
        jsonString = jsonUtil.toJson(video);
        jsonUtil.writeJsonFile(jsonString, jsonFileName);

        appList.clear();
        locationList.clear();

        //Set Resume action to Notification and update the current notification
        Intent recordStartIntent = new Intent(this, RecorderService.class);
        recordStartIntent.setAction(Const.SCREEN_RECORDING_START);
        PendingIntent precordStartIntent = PendingIntent.getService(this, 0, recordStartIntent, 0);
        if (isSkipped){
            updateNotification(createRecordingNotification(null, Const.SCREEN_RECORDING_SKIP).setUsesChronometer(false).build(), Const.SCREEN_RECORDER_NOTIFICATION_ID);
        }else{
            updateNotification(createRecordingNotification(null, Const.SCREEN_RECORDING_STOP).setUsesChronometer(false).build(), Const.SCREEN_RECORDER_NOTIFICATION_ID);
        }
        //Toast.makeText(this, R.string.screen_recording_stopped_toast, Toast.LENGTH_SHORT).show();

        if (isBound)
            floatingControlService.setRecordingState(Const.RecordingState.STOPPED);


        if (!stopped) {
            if (isBound) {
                unbindService(serviceConnection);
                Log.d(Const.TAG, "Unbinding connection service");
            }
            stopScreenSharing();

            //Send a broadcast receiver to the plugin app to disable show touches since the recording is stopped
            if (showTouches) {
                Intent TouchIntent = new Intent();
                TouchIntent.setAction("com.orpheusdroid.screenrecorder.DISABLETOUCH");
                TouchIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(TouchIntent);
            }
        }
        stopped = true;
    }


    @SuppressLint("MissingPermission")
    public void startScreenRec(Intent intent) {

        video = new Video(); // create a new video to collect data for the video recording

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(locationTAG, "Location is not granted");
            video.setLocationGranted(Boolean.FALSE);
        } else {
            Log.d(locationTAG, "Location is granted");
            video.setLocationGranted(Boolean.TRUE);
            getLastLocation();
        }


        video.setUserID(getAndroidID());
        startVideoTime = System.currentTimeMillis(); // video start time
        video.setStartTime(startVideoTime);

        if (!isRecording) {
            //Get values from Default SharedPreferences
            getValues();
            if (recordingCounter == 0) {
                data = intent.getParcelableExtra(Const.RECORDER_INTENT_DATA);
                result = intent.getIntExtra(Const.RECORDER_INTENT_RESULT, Activity.RESULT_OK);
                previousData = data;
                previousResult = result;
                recordingCounter++;
            } else {
                data = previousData;
                result = previousResult;
            }

            // Check if an app has to be started before recording and start the app
            if (prefs.getBoolean(getString(R.string.preference_enable_target_app_key), false))
                startAppBeforeRecording(prefs.getString(getString(R.string.preference_app_chooser_key), "none"));

            boolean isShakeGestureActive = prefs.getBoolean(getString(R.string.preference_shake_gesture_key), false);

            if (isShakeGestureActive) {
                //SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mShakeDetector = new ShakeEventManager(this);
                mShakeDetector.init(this);

                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher);

                Intent destroyMediaRecorderIntent = new Intent(this, RecorderService.class);
                destroyMediaRecorderIntent.setAction(Const.SCREEN_RECORDING_DESTORY_SHAKE_GESTURE);
                PendingIntent pdestroyMediaRecorderIntent = PendingIntent.getService(this, 0, destroyMediaRecorderIntent, 0);

                NotificationCompat.Builder shakeGestureWaitNotification =
                        new NotificationCompat.Builder(this, Const.RECORDING_NOTIFICATION_CHANNEL_ID)
                                .setContentTitle("Waiting for device shake")
                                .setContentText("Shake your device to start recording or press this notification to cancel")
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(
                                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                                .setContentIntent(pdestroyMediaRecorderIntent);

                startNotificationForeGround(shakeGestureWaitNotification.build(), Const.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);

                Toast.makeText(this, R.string.screenrecording_waiting_for_gesture_toast,
                        Toast.LENGTH_LONG).show();
            } else {
                startRecording();
            }

        } else {
            Log.d(Const.TAG, "It is already recording");
            Toast.makeText(this, R.string.screenrecording_already_active_toast, Toast.LENGTH_SHORT).show();
        }
    }


    // Start the selected app before recording if its enabled and an app is selected
    private void startAppBeforeRecording(String packagename) {
        if (packagename.equals("none"))
            return;

        Intent startAppIntent = getPackageManager().getLaunchIntentForPackage(packagename);
        startActivity(startAppIntent);
    }


    private void startRecording() {
        //Initialize MediaRecorder class and initialize it with preferred configuration

        mMediaRecorder = new MediaRecorder();
        initRecorder();

        //Set Callback for MediaProjection
        mMediaProjectionCallback = new MediaProjectionCallback();
        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        Log.d(Const.TAG, "Value of data" + String.valueOf(data));

        //Initialize MediaProjection using data received from Intent
        mMediaProjection = mProjectionManager.getMediaProjection(result, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);

        /* Create a new virtual display with the actual default display
         * and pass it on to MediaRecorder to start recording */
        mVirtualDisplay = createVirtualDisplay();
        try {
            mMediaRecorder.start();

            //If floating controls is enabled, start the floating control service and bind it here
            if (useFloatingControls) {
                Intent floatinControlsIntent = new Intent(this, FloatingControlService.class);
                startService(floatinControlsIntent);
                bindService(floatinControlsIntent,
                        serviceConnection, BIND_AUTO_CREATE);
            }

            //Set the state of the recording
            if (isBound)
                floatingControlService.setRecordingState(Const.RecordingState.RECORDING);
            isRecording = true;
            stopped = false;

            //Send a broadcast receiver to the plugin app to enable show touches since the recording is started
            if (showTouches) {
                Intent TouchIntent = new Intent();
                TouchIntent.setAction("com.orpheusdroid.screenrecorder.SHOWTOUCH");
                TouchIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(TouchIntent);
            }
        } catch (IllegalStateException e) {
            Log.d(Const.TAG, "Mediarecorder reached Illegal state exception. Did you start the recording twice?");
            Toast.makeText(this, R.string.recording_failed_toast, Toast.LENGTH_SHORT).show();
            isRecording = false;
        }

        /* Add Pause action to Notification to pause screen recording if the video's android version
         * is >= Nougat(API 24) since pause() isnt available previous to API24 else build
         * Notification with only default stop() action
         * I did not include pause in this code */

            //startTime is to calculate elapsed recording time to update notification during pause/resume
            startTime = System.currentTimeMillis();
            Intent recordPauseIntent = new Intent(this, RecorderService.class);
            recordPauseIntent.setAction(Const.SCREEN_RECORDING_SKIP);
            PendingIntent precordPauseIntent = PendingIntent.getService(this, 0, recordPauseIntent, 0);
            NotificationCompat.Action action = new NotificationCompat.Action(0,
                    getString(R.string.screen_recording_notification_action_pause), precordPauseIntent);

            //Start Notification as foreground
            startNotificationForeGround(createRecordingNotification(action, Const.SCREEN_RECORDING_START).build(), Const.SCREEN_RECORDER_NOTIFICATION_ID);

    }

    //Virtual display created by mirroring the actual physical display
    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                WIDTH, HEIGHT, DENSITY_DPI,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    /* Initialize MediaRecorder with desired default values and values set by video. Everything is
     * pretty much self explanatory */
    private void initRecorder() {
        try {
            if (mustRecAudio)
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(SAVEPATH);
            mMediaRecorder.setVideoSize(WIDTH, HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (mustRecAudio)
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncodingBitRate(BITRATE);
            mMediaRecorder.setVideoFrameRate(FPS);
            int rotation = window.getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Add notification channel for supporting Notification in Api 26 (Oreo)
    @TargetApi(26)
    private void createNotificationChannels() {
        List<NotificationChannel> notificationChannels = new ArrayList<>();
        NotificationChannel recordingNotificationChannel = new NotificationChannel(
                Const.RECORDING_NOTIFICATION_CHANNEL_ID,
                Const.RECORDING_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        recordingNotificationChannel.enableLights(true);
        recordingNotificationChannel.setLightColor(Color.RED);
        recordingNotificationChannel.setShowBadge(true);
        recordingNotificationChannel.enableVibration(false);
        recordingNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannels.add(recordingNotificationChannel);

        NotificationChannel shareNotificationChannel = new NotificationChannel(
                Const.SHARE_NOTIFICATION_CHANNEL_ID,
                Const.SHARE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        shareNotificationChannel.enableLights(true);
        shareNotificationChannel.setLightColor(Color.RED);
        shareNotificationChannel.setShowBadge(true);
        shareNotificationChannel.enableVibration(false);
        shareNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannels.add(shareNotificationChannel);

        getManager().createNotificationChannels(notificationChannels);
    }

    /* Create Notification.Builder with action passed in case video's android version is greater than
     * API24 */
    private NotificationCompat.Builder createRecordingNotification(NotificationCompat.Action
                                                                           action, String recordingState) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        startTime = System.currentTimeMillis();
        Intent recordStartIntent = new Intent(this, RecorderService.class);
        recordStartIntent.setAction(Const.SCREEN_RECORDING_START);
        PendingIntent precordStartIntent = PendingIntent.getService(this, 0, recordStartIntent, 0);
        NotificationCompat.Action actionStart = new NotificationCompat.Action(0,
                getString(R.string.screen_recording_notification_action_start), precordStartIntent);


        Intent recordStopIntent = new Intent(this, RecorderService.class);
        recordStopIntent.setAction(Const.SCREEN_RECORDING_STOP);
        PendingIntent precordStopIntent = PendingIntent.getService(this, 0, recordStopIntent, 0);

        Intent UIIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationContentIntent = PendingIntent.getActivity(this, 0, UIIntent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, Const.RECORDING_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setUsesChronometer(true)
                .setOngoing(true)
                .setContentIntent(notificationContentIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(actionStart)
                .setOnlyAlertOnce(true)
                .setGroup(Const.GROUP_KEY_WORK_Recorder)
                .addAction(0, getResources().getString(R.string.screen_recording_notification_action_stop),
                        precordStopIntent);

//        addAction(R.drawable.ic_notification_stop, getResources().getString(R.string.screen_recording_notification_action_stop),
//                precordStopIntent);

        switch (recordingState) {
            case Const.SCREEN_RECORDING_SKIP:
                notification.setContentTitle(getResources().getString(R.string.screen_skip_notification_title))
                        .setTicker(getResources().getString(R.string.screen_skip_notification_title));
                break;
            case Const.SCREEN_RECORDING_RESUME:
                notification.setContentTitle(getResources().getString(R.string.screen_recording_notification_title))
                        .setTicker(getResources().getString(R.string.screen_recording_notification_title));
                break;
            case Const.SCREEN_RECORDING_STOP:
                notification.setContentTitle(getResources().getString(R.string.screen_stop_notification_title))
                        .setTicker(getResources().getString(R.string.screen_recording_notification_title));
                break;
            default:
                notification.setContentTitle(getResources().getString(R.string.screen_recording_notification_title))
                        .setTicker(getResources().getString(R.string.screen_recording_notification_title));
        }

        if (action != null)
            notification.addAction(action);

        if (isBound)
            floatingControlService.setRecordingState(Const.RecordingState.STOPPED);
        return notification;
    }

    private void showShareNotification() {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Uri videoUri = FileProvider.getUriForFile(
                this, this.getApplicationContext().getPackageName() + ".provider",
                new File(SAVEPATH));

        Intent Shareintent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, videoUri)
                .setType("video/mp4");

        Intent editIntent = new Intent(this, EditVideoActivity.class);
        editIntent.putExtra(Const.VIDEO_EDIT_URI_KEY, SAVEPATH);
        PendingIntent editPendingIntent = PendingIntent.getActivity(this, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent sharePendingIntent = PendingIntent.getActivity(this, 0, Intent.createChooser(
                Shareintent, getString(R.string.share_intent_title)), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder shareNotification = new NotificationCompat.Builder(this, Const.SHARE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.share_intent_notification_title))
                .setContentText(getString(R.string.share_intent_notification_content))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(sharePendingIntent)
                .addAction(android.R.drawable.ic_menu_share, getString(R.string.share_intent_notification_action_text)
                        , sharePendingIntent)
                .addAction(android.R.drawable.ic_menu_edit, getString(R.string.edit_intent_notification_action_text)
                        , editPendingIntent);
        updateNotification(shareNotification.build(), Const.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
    }

    //Start service as a foreground service. We dont want the service to be killed in case of low memory
    private void startNotificationForeGround(Notification notification, int ID) {
        startForeground(ID, notification);
    }

    //Update existing notification with its ID and new Notification data
    private void updateNotification(Notification notification, int ID) {
        getManager().notify(ID, notification);
    }

    private NotificationManager getManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    @Override
    public void onDestroy() {
        Log.d(Const.TAG, "Recorder service destroyed");
        super.onDestroy();
        unregisterReceiver(chargerReceiver);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(Const.TAG, "OnAccessibility");

        if (event.getPackageName() == null) {
            Log.d(Const.TAG, "Retrun ************** Package manager");
            return;
        }

        PackageManager packageManager = getPackageManager();
        ApplicationInfo appInfo;
        PackageInfo pkgInfo;

        try {
            appInfo = packageManager.getApplicationInfo(event.getPackageName().toString(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException | NullPointerException | Resources.NotFoundException e) {
            appInfo = null;
        }

        try {
            pkgInfo = packageManager.getPackageInfo(event.getPackageName().toString(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException | NullPointerException | Resources.NotFoundException e) {
            pkgInfo = null;
        }


        String appName = "";
        try {
            if (appInfo != null) {
                appName = packageManager.getApplicationLabel(appInfo).toString();
            }
        } catch (Resources.NotFoundException | NullPointerException e) {
            appName = "";
        }

        Log.d(Const.TAG, " App list ");
        app = new App();
        try {
            if (app != null) {
                app.setAppName(appName);
                app.setTimeStamp(System.currentTimeMillis());
                appList.add(app);
                Log.d(Const.TAG, "App name: " + appName);
                Log.d(Const.TAG, "Event: " + event.getPackageName().toString());
                Log.d(Const.TAG, "Time stamp: " + String.valueOf(System.currentTimeMillis()));
            }
        } catch (Exception e) {
            Log.d(Const.TAG, e.toString());
        }
    }

    @Override
    public void onInterrupt() {

    }

    //Get video's choices for video choosable settings
    public void getValues() {
        String res = prefs.getString(getString(R.string.res_key), getResolution());
        setWidthHeight(res);
        FPS = Integer.parseInt(prefs.getString(getString(R.string.fps_key), "30"));
        BITRATE = Integer.parseInt(prefs.getString(getString(R.string.bitrate_key), "7130317"));
        mustRecAudio = prefs.getBoolean(getString(R.string.audiorec_key), false);
        String saveLocation = prefs.getString(getString(R.string.savelocation_key),
                Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
        File saveDir = new File(saveLocation);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !saveDir.isDirectory()) {
            saveDir.mkdirs();
        }
        useFloatingControls = prefs.getBoolean(getString(R.string.preference_floating_control_key), false);
        showTouches = prefs.getBoolean(getString(R.string.preference_show_touch_key), false);
        String saveFileName = getFileSaveName();
        jsonFileName = saveFileName;
        SAVEPATH = saveLocation + File.separator + saveFileName + ".mp4";
    }

    /* The PreferenceScreen save values as string and we save the video selected video resolution as
     * WIDTH x HEIGHT. Lets split the string on 'x' and retrieve width and height */
    private void setWidthHeight(String res) {
        String[] widthHeight = res.split("x");
        WIDTH = Integer.parseInt(widthHeight[0]);
        HEIGHT = Integer.parseInt(widthHeight[1]);
    }

    //Get the device resolution in pixels
    private String getResolution() {
        DisplayMetrics metrics = new DisplayMetrics();
        window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        window.getDefaultDisplay().getMetrics(metrics);
        DENSITY_DPI = metrics.densityDpi;
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return width + "x" + height;
    }

    //Return filename of the video to be saved formatted as chosen by the video
    private String getFileSaveName() {
        String filename = prefs.getString(getString(R.string.filename_key), "yyyyMMdd_hhmmss");
        String prefix = prefs.getString(getString(R.string.fileprefix_key), "recording");
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(filename);
        return prefix + "_" + formatter.format(today);
    }

    //Stop and destroy all the objects used for screen recording
    private void destroyMediaProjection() {
        try {
            mMediaRecorder.stop();
            indexFile();
            Log.i(Const.TAG, "MediaProjection Stopped");

        } catch (RuntimeException e) {
            Log.e(Const.TAG, "Fatal exception! Destroying media projection failed." + "\n" + e.getMessage());
            if (new File(SAVEPATH).delete())
                Log.d(Const.TAG, "Corrupted file delete successful");
            Toast.makeText(this, getString(R.string.fatal_exception_message), Toast.LENGTH_SHORT).show();
        } finally {
            mMediaRecorder.reset();
            mVirtualDisplay.release();
            mMediaRecorder.release();
            if (mMediaProjection != null) {
                mMediaProjection.unregisterCallback(mMediaProjectionCallback);
                mMediaProjection.stop();
                mMediaProjection = null;
            }
        }
        isRecording = false;
    }

    /* Its weird that android does not index the files immediately once its created and that causes
     * trouble for video in finding the video in gallery. Let's explicitly announce the file creation
     * to android and index it */
    private void indexFile() {
        //Create a new ArrayList and add the newly created video file path to it
        ArrayList<String> toBeScanned = new ArrayList<>();
        toBeScanned.add(SAVEPATH);
        String[] toBeScannedStr = new String[toBeScanned.size()];
        toBeScannedStr = toBeScanned.toArray(toBeScannedStr);

        //Request MediaScannerConnection to scan the new file and index it
        MediaScannerConnection.scanFile(this, toBeScannedStr, null, new MediaScannerConnection.OnScanCompletedListener() {

            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.i(Const.TAG, "SCAN COMPLETED: " + path);
                //Show toast on main thread
                Message message = mHandler.obtainMessage();
                message.sendToTarget();
                //stopSelf();
            }
        });
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            Log.d(Const.TAG, "Virtual display is null. Screen sharing already stopped");
            return;
        }
        destroyMediaProjection();
    }

    @Override
    public void onShake() {
        if (!isRecording) {
            Vibrator vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            getManager().cancel(Const.SCREEN_RECORDER_WAITING_FOR_SHAKE_NOTIFICATION_ID);

            startRecording();
            Toast.makeText(this, "Rec start", Toast.LENGTH_SHORT).show();
        } else {
            Intent recordStopIntent = new Intent(this, RecorderService.class);
            recordStopIntent.setAction(Const.SCREEN_RECORDING_STOP);
            startService(recordStopIntent);
            Toast.makeText(this, "Rec stop", Toast.LENGTH_SHORT).show();
            mShakeDetector.stop();
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.v(Const.TAG, "Recording Stopped");
            stopScreenSharing();
        }
    }
}