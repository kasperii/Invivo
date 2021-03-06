/*
 * Copyright (c) 2016-2017. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.orpheusdroid.screenrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.legacy.app.FragmentStatePagerAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;
import ly.count.android.sdk.DeviceId;

import static com.orpheusdroid.screenrecorder.BeaconRecorderApplication.getIsRecording;


public class MainActivity extends AppCompatActivity {

  private PermissionResultListener mPermissionResultListener;
  private AnalyticsSettingsListerner analyticsSettingsListerner;
  private MediaProjection mMediaProjection;
  private MediaProjectionManager mProjectionManager;
  private FloatingActionButton fab;
  private FloatingActionButton fab2;
  private TabLayout tabLayout;
  private ViewPager viewPager;
  private SharedPreferences prefs;
  private LocationManager locationManager;
  Intent intent;
  private boolean recordingIsON = false;
  SharedPreferences statePrefs;

  private static final String TAG = "MainActivity";
  private static final String EXTRA_STORAGE_REFERENCE_KEY = "StorageReference";
  private BeaconRecorderApplication myApp;

  //Method to create app directory which is default directory for storing recorded videos
  public static void createDir() {
    //TODO: ADD SUPPORT FOR version 28
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
      File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !appDir.isDirectory()) {
        appDir.mkdirs();
      }}else{
      File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !appDir.isDirectory()) {
        boolean x = appDir.mkdirs();
        Log.d("is: ", String.valueOf(x));
        //CHECK NULL=?

    }
  }}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    myApp = ((BeaconRecorderApplication) getApplicationContext());
    // intent = new Intent(MainActivity.this, RecorderService.class);

    Intent intent = getIntent();
    String theme = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.preference_theme_key), Const.PREFS_LIGHT_THEME);
    int popupOverlayTheme = 0;
    int toolBarColor = 0;

    switch (theme) {
      case Const.PREFS_DARK_THEME:
        setTheme(R.style.AppTheme_Dark_NoActionBar);
        popupOverlayTheme = R.style.AppTheme_PopupOverlay_Dark;
        toolBarColor = ContextCompat.getColor(this, R.color.colorPrimary_dark);
        break;
      case Const.PREFS_BLACK_THEME:
        setTheme(R.style.AppTheme_Black_NoActionBar);
        popupOverlayTheme = R.style.AppTheme_PopupOverlay_Black;
        toolBarColor = ContextCompat.getColor(this, R.color.colorPrimary_black);
        break;
    }
    statePrefs = getSharedPreferences("Beacon", Context.MODE_PRIVATE);


    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setBackgroundColor(toolBarColor);

    if (popupOverlayTheme != 0)
      toolbar.setPopupTheme(popupOverlayTheme);

    setSupportActionBar(toolbar);

    viewPager = findViewById(R.id.viewpager);
    setupViewPager(viewPager);

    tabLayout = findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setBackgroundColor(toolBarColor);

    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    //Arbitrary "Write to external storage" permission since this permission is most important for the app
    requestPermissionStorage();
    requestPermissionAudio();

    //Acquiring media projection service to start screen mirroring
    mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    //Respond to app shortcut
    if (getIntent().getAction() != null && getIntent().getAction().equals(getString(R.string.app_shortcut_action))) {
      startActivityForResult(mProjectionManager.createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);
      return;
    } else if (getIntent().getScheme() != null && getIntent().getScheme().equals("invivo")) {
      if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        Uri uri = intent.getData();
        Log.d(Const.TAG, "URI: "+uri);
        Log.d(Const.TAG, "Intent: "+ getIntent().toString());

        final String selector = uri.getQueryParameter("selector");
        final String token = uri.getQueryParameter("token");
        final String udid = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        new Thread(new Runnable() {
          @Override
          public void run() {
            URL yahoo = null;

            try {
              yahoo = new URL("https://invivo.dsv.su.se/linkaccount.php?selector=" + selector + "&token=" + token + "&udid=" + udid);
              Log.d(Const.TAG, "Yahooo: " + yahoo);
              Log.d(Const.TAG, "Selector "+selector);
              Log.d(Const.TAG, "token "+ token);

            } catch (MalformedURLException e) {
              Log.d(Const.TAG, "MalformedURL" + e.toString());
            }
            BufferedReader in = null;
            try {
              in = new BufferedReader(
                      new InputStreamReader(
                              yahoo.openStream()));
            } catch (Exception e) {
              Log.d(Const.TAG, "EXCEPTION BufferedReader" + e.toString());
            }

            String inputLine;
            try {
              while ((inputLine = in.readLine()) != null)
                Log.d(Const.TAG, "Reply from server: " + inputLine);
              in.close();
            } catch (Exception e) {
              Log.d(Const.TAG, "EXCEPTION Inputline" + e.toString());
            }
          }
        }).start();
      }

    }

    fab = findViewById(R.id.fab);
    fab2 = findViewById(R.id.fab2);
    if (mMediaProjection != null) {
      Log.d(TAG, "on click");
      fab.hide();
    }

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(Const.TAG, "Recording Service is running: " + String.valueOf(isServiceRunning(RecorderService.class)));
        if (mMediaProjection == null && !getIsRecording()) { //&& !isServiceRunning(RecorderService.class)
          //Request Screen recording permission
          Log.d(Const.TAG, "on mMediaProjection");
          startActivityForResult(mProjectionManager.createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);

        } else if (isServiceRunning(RecorderService.class)) {
          Log.d(Const.TAG, "Recording service Is running");
        }
      }
    });
    fab.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        Toast.makeText(MainActivity.this, R.string.fab_record_hint, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
    fab2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
          ((BeaconTrackerFragment) adapter.getItem(2)).addNewItem();
      }
    });
    fab2.hide();
  }


  public void setupAnalytics() {
    if (!prefs.getBoolean(getString(R.string.preference_crash_reporting_key), false) &&
            !prefs.getBoolean(getString(R.string.preference_anonymous_statistics_key), false)) {
      Log.d(Const.TAG, "Analytics disabled by user");
      return;
    }
    Countly.sharedInstance().init(this, Const.ANALYTICS_URL, Const.ANALYTICS_API_KEY,
            null, DeviceId.Type.OPEN_UDID, 3, null, null, null, null);
    Countly.sharedInstance().setHttpPostForced(true);
    Countly.sharedInstance().enableParameterTamperingProtection(getPackageName());

    if (prefs.getBoolean(getString(R.string.preference_crash_reporting_key), false)) {
      Countly.sharedInstance().enableCrashReporting();
      Log.d(Const.TAG, "Enabling crash reporting");
    }
    if (prefs.getBoolean(getString(R.string.preference_anonymous_statistics_key), false)) {
      Countly.sharedInstance().setStarRatingDisableAskingForEachAppVersion(false);
      Countly.sharedInstance().setViewTracking(true);
      Countly.sharedInstance().setIfStarRatingShownAutomatically(true);
      Log.d(Const.TAG, "Enabling countly statistics");
    }
    Countly.sharedInstance().onStart(this);
  }

  private void setupViewPager(ViewPager viewPager) {
    ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
    adapter.addFragment(new SettingsPreferenceFragment(), getString(R.string.tab_settings_title));
    adapter.addFragment(new VideosListFragment(), getString(R.string.tab_videos_title));
    adapter.addFragment(new BeaconTrackerFragment(), getString(R.string.tab_beacon_title));
    viewPager.setAdapter(adapter);
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        switch (position) {
          case 0:
            fab.show();
            fab2.hide();
            break;
          case 1:
              fab.hide();
              fab2.hide();
              break;
          case 2:
            fab.hide();
            fab2.show();
            break;
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });
  }

  //Method to check if the service is running
  private boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  //Overriding onActivityResult to capture screen mirroring permission request result
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    //Result for system windows permission required to show floating controls
    if (requestCode == Const.SYSTEM_WINDOWS_CODE) {
      setSystemWindowsPermissionResult();
      return;
    }

    //The user has denied permission for screen mirroring. Let's notify the user
    if (resultCode == RESULT_CANCELED && requestCode == Const.SCREEN_RECORD_REQUEST_CODE) {
      Toast.makeText(this,
              getString(R.string.screen_recording_permission_denied), Toast.LENGTH_SHORT).show();
      //Return to home screen if the app was started from app shortcut
      if (getIntent().getAction().equals(getString(R.string.app_shortcut_action)))
        this.finish();
      return;
    }

    /*If code reaches this point, congratulations! The user has granted screen mirroring permission
     * Let us set the recorderservice intent with relevant data and start service*/
    myApp.setScreenshotPermission(requestCode, resultCode, (Intent) data.clone());
    Intent recorderService = new Intent(this, RecorderService.class);
    recorderService.setAction(Const.SCREEN_RECORDING_START);
    recorderService.putExtra(Const.RECORDER_INTENT_DATA, data);
    recorderService.putExtra(Const.RECORDER_INTENT_RESULT, resultCode);
    startService(recorderService);
    this.finish();

    //Start ScreenReceiver, which makes the the recorder works all the time in screen on
    Intent screenServiceIntent = new Intent(this, ScreenService.class);
    startService(screenServiceIntent);
    recordingIsON = true;
    super.onActivityResult(requestCode, resultCode, data);
  }


  //Update video list fragment once save directory has been changed
  public void onDirectoryChanged() {
    ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
    ((VideosListFragment) adapter.getItem(1)).removeVideosList();
    Log.d(Const.TAG, "reached main act");
  }

  /* Marshmallow style permission request.
   * We also present the user with a dialog to notify why storage permission is required */
  public boolean requestPermissionStorage() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
      AlertDialog.Builder alert = new AlertDialog.Builder(this)
              .setTitle(getString(R.string.storage_permission_request_title))
              .setMessage(getString(R.string.storage_permission_request_summary))
              .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  ActivityCompat.requestPermissions(MainActivity.this,
                          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                          Const.EXTDIR_REQUEST_CODE);
                }
              })
              .setCancelable(false);

      alert.create().show();
      return false;
    }
    return true;
  }

  //Check location permission
  public void requestPermissionLocation() {
    if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)) {
      //permission is not granted
      ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET},
              Const.LOCATION_REQUEST_CODE);
      statusCheck();
    } else {
      //permission has already been granted
      statusCheck();
      Log.d(Const.TAG, "Location is already granted");
    }

  }

  public void statusCheck() {
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      buildAlertMessageNoGps();
    }
  }

  private void buildAlertMessageNoGps() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
              }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
              }
            });
    final AlertDialog alert = builder.create();
    alert.show();
  }


  //Permission on api below 23 are granted by default
  @TargetApi(23)
  public void requestSystemWindowsPermission() {
    if (!Settings.canDrawOverlays(this)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
              Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, Const.SYSTEM_WINDOWS_CODE);
    }
  }

  //Pass the system windows permission result to settings fragment
  @TargetApi(23)
  private void setSystemWindowsPermissionResult() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (Settings.canDrawOverlays(this)) {
        mPermissionResultListener.onPermissionResult(Const.SYSTEM_WINDOWS_CODE,
                new String[]{"System Windows Permission"},
                new int[]{PackageManager.PERMISSION_GRANTED});
      } else {
        mPermissionResultListener.onPermissionResult(Const.SYSTEM_WINDOWS_CODE,
                new String[]{"System Windows Permission"},
                new int[]{PackageManager.PERMISSION_DENIED});
      }
    } else {
      mPermissionResultListener.onPermissionResult(Const.SYSTEM_WINDOWS_CODE,
              new String[]{"System Windows Permission"},
              new int[]{PackageManager.PERMISSION_GRANTED});
    }
  }

  // Marshmallow style permission request for audio recording
  public void requestPermissionAudio() {
    if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
              new String[]{Manifest.permission.RECORD_AUDIO},
              Const.AUDIO_REQUEST_CODE);
    }
  }


  // Overriding onRequestPermissionsResult method to receive results of marshmallow style permission request
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {

      case Const.EXTDIR_REQUEST_CODE:
        if ((grantResults.length > 0) &&
                (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
          Log.d(Const.TAG, "write storage Permission Denied");
          /* Disable floating action Button in case write storage permission is denied.
           * There is no use in recording screen when the video is unable to be saved */
          fab.setEnabled(false);
        } else {
          /* Since we have write storage permission now, lets create the app directory
           * in external storage*/
          Log.d(Const.TAG, "write storage Permission granted");
          createDir();
        }

//            case Const.LOCATION_REQUEST_CODE:
//                if ((grantResults.length > 0) &&
//                        (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
//                    Log.d(Const.TAG, "Location service is not granted");
//                } else {
//                    Log.d(Const.TAG, "get location");
//              }
    }

    // Let's also pass the result data to SettingsPreferenceFragment using the callback interface
    if (mPermissionResultListener != null) {
      mPermissionResultListener.onPermissionResult(requestCode, permissions, grantResults);
    }

  }


  private void requestAnalyticsPermission() {
    if (!prefs.getBoolean(Const.PREFS_REQUEST_ANALYTICS_PERMISSION, true))
      return;

    new AlertDialog.Builder(this)
            .setTitle("Allow anonymous analytics")
            .setMessage("Do you want to allow anonymous crash reporting and usage metrics now?" +
                    "\nRead the privacy policy to know more on what data are collected")
            .setPositiveButton("Enable analytics", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                analyticsSettingsListerner.updateAnalyticsSettings(Const.analytics.CRASHREPORTING);
                analyticsSettingsListerner.updateAnalyticsSettings(Const.analytics.USAGESTATS);
                prefs.edit()
                        .putBoolean(Const.PREFS_REQUEST_ANALYTICS_PERMISSION, false)
                        .apply();
              }
            })
            .setNeutralButton("Enable Crash reporting only", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                analyticsSettingsListerner.updateAnalyticsSettings(Const.analytics.CRASHREPORTING);
                prefs.edit()
                        .putBoolean(Const.PREFS_REQUEST_ANALYTICS_PERMISSION, false)
                        .apply();
              }
            })
            .setNegativeButton("Disable everything", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                prefs.edit()
                        .putBoolean(Const.PREFS_REQUEST_ANALYTICS_PERMISSION, false)
                        .apply();
              }
            })
            .setCancelable(false)
            .create().show();
  }

  //Set the callback interface for permission result from SettingsPreferenceFragment
  public void setPermissionResultListener(PermissionResultListener mPermissionResultListener) {
    this.mPermissionResultListener = mPermissionResultListener;
  }

  public void setAnalyticsSettingsListerner(AnalyticsSettingsListerner
                                                    analyticsSettingsListerner) {
    this.analyticsSettingsListerner = analyticsSettingsListerner;
  }

  @Override
  protected void onStart() {
    super.onStart();
    /* Enable analytics only for release builds */
    if (!BuildConfig.DEBUG) {
      Log.d(Const.TAG, "Is a release build. Setting up analytics");
      requestAnalyticsPermission();
      setupAnalytics();
    } else {
      Log.d(Const.TAG, "Debug build. Analytics is disabled");
    }
  }

  @Override
  protected void onStop() {
    if (!BuildConfig.DEBUG) {
      if (Countly.sharedInstance().hasBeenCalledOnStart()) {
        Countly.sharedInstance().onStop();
        Log.d(Const.TAG, "Countly stopped");
      }
    }
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.overflow_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case R.id.privacy_policy:
        startActivity(new Intent(this, PrivacyPolicy.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public interface AnalyticsSettingsListerner {
    void updateAnalyticsSettings(Const.analytics analytics);
  }

  //ViewPager class for tab view
  class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    ViewPagerAdapter(FragmentManager manager) {
      super(manager);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
      return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
      return mFragmentList.size();
    }

    void addFragment(Fragment fragment, String title) {
      mFragmentList.add(fragment);
      mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mFragmentTitleList.get(position);
    }
  }
}