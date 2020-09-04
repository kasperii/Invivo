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
public class AcquireScreenshotPermissionIntent extends Activity {

    private Context context;
    private static MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private BeaconRecorderApplication myApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("AcquireScreenshotPermissionIntent", "onCreate");
        myApp = ((BeaconRecorderApplication) getApplicationContext());
        super.onCreate(savedInstanceState);
        //mediaProjectionManager = getmProjectionManager();
        //mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(myApp.getmProjectionManager().createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("AcquireScreenshotPermissionIntent", "Opened");
        if (1003 == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                //myApp.makeToastHere("ScreenshtPermisIntent - ok !");
                myApp.setScreenshotPermission(requestCode, resultCode, (Intent) data.clone());
            }
        } else if (Activity.RESULT_CANCELED == resultCode) {
            Log.d("AcquireScreenshotPermissionIntent","no access");
            //myApp.makeToastHere("ScreenshtPermisIntent - no !");
        }
        super.onActivityResult(requestCode, resultCode, data);
        myApp = null;
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("AcquireScreenshotPermissionIntent", "onDestroy: ");
        myApp = null;
        //context = null;
    }




    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
