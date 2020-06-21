package com.orpheusdroid.screenrecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import static com.orpheusdroid.screenrecorder.BeaconRecorderApplication.getmProjectionManager;
import static com.orpheusdroid.screenrecorder.BeaconRecorderApplication.setScreenshotPermission;
public class AcquireScreenshotPermissionIntent extends Activity {

    private Context context;
    private static MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    static boolean acquiringScreenshotPermissionIntent = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaProjectionManager = getmProjectionManager();
        mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);
        acquiringScreenshotPermissionIntent = true;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("AcquireScreenshotPermissionIntent", "Opened");
        super.onActivityResult(requestCode, resultCode, data);
        if (1003 == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                setScreenshotPermission(requestCode, resultCode, (Intent) data.clone());
            }
        } else if (Activity.RESULT_CANCELED == resultCode) {
            setScreenshotPermission(requestCode, resultCode, null);
            Log.d("AcquireScreenshotPermissionIntent","no access");

        }
        acquiringScreenshotPermissionIntent = false;
        this.finish();
    }




    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
