package com.orpheusdroid.screenrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyPowerReceiver extends BroadcastReceiver{


    private static final String TAG = "MyPowerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if(action.equals(Intent.ACTION_POWER_CONNECTED))
        {
            Log.d(TAG, "Power connected ");
            MyBroadcast.startService(context);
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            Log.d(TAG, "Power not connected ");
            MyBroadcast.startService(context);
        }

        // Toast.makeText(context, "Power changes...", Toast.LENGTH_LONG).show();
    }

}
