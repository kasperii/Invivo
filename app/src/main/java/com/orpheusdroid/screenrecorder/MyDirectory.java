package com.orpheusdroid.screenrecorder;

import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;


/**
 * Created by razan on 2017-10-12.
 */

public class MyDirectory {

    public static String path = Environment.getExternalStorageDirectory().toString() + "/screenrecorder";
    private static final String TAG = "MyDirectory";
    private static FileObserver fileObserver;
    private static boolean deleted = false;

    public static boolean isDeleted(final String fName) {
        fileObserver = new FileObserver(path) {
            @Override
            public void onEvent(int event, String fDeleted) {
                if ((FileObserver.DELETE & event) != 0) {
                    if (fName.equals(fDeleted)) {
                        Log.d(TAG, "We are in the observer");
                        Log.d(TAG, "FileObserver: " + event + " " + "filePath: " + fDeleted);
                        deleted = true;
                    } else {
                        Log.d(TAG, "Other file is deleted" + fDeleted);
                    }
                }
            }
        };
        fileObserver.startWatching();
        return deleted;
    }


}
