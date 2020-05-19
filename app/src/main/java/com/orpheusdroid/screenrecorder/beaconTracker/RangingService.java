package com.orpheusdroid.screenrecorder.beaconTracker;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.orpheusdroid.screenrecorder.MyNotification;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class RangingService extends Service implements BeaconConsumer {
	private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    protected static final String TAG = "RangingService";


    @Override
    public void onCreate() {
        Log.d("onCreate","hello");
        MyNotification.createNotification(getApplicationContext(), 0,"start create rang", "testString");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStartCommand","hello");
        MyNotification.createNotification(getApplicationContext(), 0,"on start create rang", "testString");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Imported method from example
    @Override
    public void onBeaconServiceConnect() {
	    //makeToastHere("on beacon service connect: Ranging");
        MyNotification.createNotification(getApplicationContext(), 0,"connected ranging", "testString");

        RangeNotifier rangeNotifier = new RangeNotifier() {
           @Override
           public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
              if (beacons.size() > 0) {
                  Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                  Beacon firstBeacon = beacons.iterator().next();
                  //this method is not available anymore - better display updates another way
                  Log.d(TAG, "The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                  //logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                  MyNotification.createNotification(getApplicationContext(), 0,createRangingStringOut(firstBeacon.getDistance()), "testString");

              }
           }

        };
        try {
            MyNotification.createNotification(getApplicationContext(), 0,"startRanding", "testString");

            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {   }
    }
    public String createRangingStringOut(double dist){
        String s = Double.toString(dist);
        s = s.concat(" + ");
//        s = s.concat(" current state: ");
//        s = s.concat(Integer.toString(0));
        s = s.concat(" + call from:");
        s = s.concat(" ranging");
        return s;
    }

//    public void makeToastHere(String text){
//        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
//        toast.show();
//    }

//    public void ShowToastInIntentService(final String sText) {
//        final Context MyContext = this;
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Toast toast1 = Toast.makeText(MyContext, sText, Toast.LENGTH_LONG);
//                toast1.show();
//            }
//        });
//    };
}
