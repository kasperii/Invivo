package com.orpheusdroid.screenrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class MyNetworkReceiver extends BroadcastReceiver {

    public static final String TAG = "MyNetworkReceiver";

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Bundle bundle = intent.getExtras();

        if (bundle == null){
            return;
        }

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (null != activeNetwork) {
            if (NetworkUtil.isWifiConnected(context) || NetworkUtil.isConnected(context)) {
                Log.d(TAG, "connected!");
                MyBroadcast.startService(context);
            } else {
                MyBroadcast.startService(context);
                return;
            }
        }else {
            return;
        }
    }
}
