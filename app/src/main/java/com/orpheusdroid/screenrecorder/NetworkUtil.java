package com.orpheusdroid.screenrecorder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by razan on 2017-09-25.
 */


public class NetworkUtil {

    public static final String TAG = "MyNetwork";

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return Const.TYPE_WIFI;
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return Const.TYPE_MOBILE;
            }
        }
        return Const.TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        String status = null;
        if (conn == Const.TYPE_WIFI) {
            status = "Wifi";
        } else if (conn == Const.TYPE_MOBILE) {
            status = "Mobile";
        } else if (conn == Const.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }


    public static boolean isWifiConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        int conn = NetworkUtil.getConnectivityStatus(context);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {

            if (activeNetwork.isConnectedOrConnecting() && conn == Const.TYPE_WIFI) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

}
