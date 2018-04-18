package com.orpheusdroid.screenrecorder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcast {

    public static final String TAG = "MyBroadcast";

    public static void startService(Context context) {

        Intent intent = new Intent(context, UploaderService.class);

        if ((NetworkUtil.isWifiConnected(context) || NetworkUtil.isConnected(context)) && Power.isCharging(context)) {
            Log.d(TAG, "Start the service...");
            context.startService(intent);
        } else if (NetworkUtil.isWifiConnected(context) || NetworkUtil.isConnected(context)) {
            Log.d(TAG, "WIFI...");
            if (Power.isCharging(context)) {
                Log.d(TAG, "WIFI AND POWER, START...");
                context.startService(intent);

            }
        } else if (Power.isCharging(context)) {
            Log.d(TAG, "POWER...");
            if (NetworkUtil.isWifiConnected(context) || NetworkUtil.isConnected(context)) {
                Log.d(TAG, "POWER AND WIFI, START...");
                context.startService(intent);
            }

        } else {
            Log.d(TAG, "NO POWER, NO WIFI...");
        }
    }


}


