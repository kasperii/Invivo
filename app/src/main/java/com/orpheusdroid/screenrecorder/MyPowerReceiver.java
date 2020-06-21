package com.orpheusdroid.screenrecorder;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

public class MyPowerReceiver extends BroadcastReceiver{


    private static final String TAG = "MyPowerReceiver";

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {


        String action = intent.getAction();

        if(action.equals(Intent.ACTION_POWER_CONNECTED))
        {
            Log.d(TAG, "Power connected ");
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            Log.d(TAG, "Power not connected ");
            Intent uploaderStopIntent = new Intent(context, UploaderService.class);
            uploaderStopIntent.setAction(Const.FILE_UPLOADING_STOP);
            context.startService(uploaderStopIntent);
        }
    }

}
