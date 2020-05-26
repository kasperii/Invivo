package com.orpheusdroid.screenrecorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//TODO: edit name to StateReceiver
public class ScreenReceiver extends BroadcastReceiver {
    private boolean isRecording;

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e("Log", "The screen is on.");
            Intent recordStartIntent = new Intent(context, RecorderService.class);
            //recordStopIntent.setAction(Const.SCREEN_RECORDING_RESUME);
            recordStartIntent.setAction(Const.SCREEN_RECORDING_RESTART);
            context.startService(recordStartIntent);

        } else {
            Log.e("Log", "The screen is off.");
            isRecording = false;
            Intent recordSkipIntent = new Intent(context, RecorderService.class);
            recordSkipIntent.setAction(Const.SCREEN_RECORDING_SKIP);
            context.startService(recordSkipIntent);
        }
    }

    //TODO: add if Intent.ACTION_ENTER_BEACON
    //TODO: add if Intent.ACTION_EXIT_BEACON
    //these values should be stored in application instance maybe? where can the last state be stored?
}
