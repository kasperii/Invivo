package com.orpheusdroid.screenrecorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
    private boolean isRecording;
    SharedPreferences statePrefs;
    private BeaconRecorderApplication myApp;

    public void onReceive(Context context, Intent intent) {
        //old solution, that resulted in many unwanted recordings happening
        //SharedPreferences statePrefs = context.getSharedPreferences("Beacon", Context.MODE_PRIVATE);
        //boolean shouldRecord = statePrefs.getBoolean("shouldRecord",false)
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e("Log", "The screen is on.");
//            Intent recordStartIntent = new Intent(context, RecorderService.class);
//
//            recordStartIntent.setAction(Const.SCREEN_RECORDING_RESTART);
//            context.startService(recordStartIntent);
            if(myApp.shouldIRecordAtScreenReceived()){
                myApp.startRecordingResetCall();
            }

        } else {
            Log.e("Log", "The screen is off.");
            isRecording = false;
//            Intent recordSkipIntent = new Intent(context, RecorderService.class);
//            recordSkipIntent.setAction(Const.SCREEN_RECORDING_SKIP);
//            context.startService(recordSkipIntent);
            myApp.stopRecordingCall();
        }
    }
}
