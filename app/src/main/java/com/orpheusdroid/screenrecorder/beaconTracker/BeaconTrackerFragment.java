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

package com.orpheusdroid.screenrecorder.beaconTracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orpheusdroid.screenrecorder.Const;
import com.orpheusdroid.screenrecorder.MyNotification;
import com.orpheusdroid.screenrecorder.R;
import com.orpheusdroid.screenrecorder.RecorderService;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;

/**
 * Created by vijai on 11-10-2016.
 */

public class BeaconTrackerFragment extends Fragment{

    private static final String TAG = "BeaconReferenceFragment";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
	private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    TrackedBeaconData[] trackedBeaconsData;
    View view;




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_beacons, container, false);

        Button startRangingButton = (Button) view.findViewById(R.id.startRanging);
        startRangingButton.setOnClickListener(new View.OnClickListener()
        {
             @Override
             public void onClick(View v)
             {
                 onRangingClicked(v);
             }
        });
        Button enableButton = (Button) view.findViewById(R.id.enableButton);
        enableButton.setOnClickListener(new View.OnClickListener()
        {
             @Override
             public void onClick(View v)
             {
                 onEnableClicked(v);
             }
        });




        //
//        recyclerView.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(getActivity());
//        recyclerView.setLayoutManager(layoutManager);
//        TrackedBeaconData[] trackedBeaconData = new TrackedBeaconData[]{
//                new TrackedBeaconData("Macbook","B0702880-A295-A8AB-F734-031A98A512DF")
//        };
        //TODO: get data from application, not hardcoded.
        //trackedBeaconsData = this.getActivity().getApplication().getTrackedBeaconsData

//        mAdapter = new beaconAddapter(trackedBeaconData);
//        recyclerView.setAdapter(mAdapter);

        //Here is hardcode example text:





        //HERE STARTS CHECK PERMISSIONs

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				if (this.getActivity().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setTitle("This app needs background location access");
						builder.setMessage("Please grant location access so this app can detect beacons in the background.");
						builder.setPositiveButton(android.R.string.ok, null);
						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@TargetApi(23)
							@Override
							public void onDismiss(DialogInterface dialog) {
								requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
										PERMISSION_REQUEST_BACKGROUND_LOCATION);
							}

						});
						builder.show();
					}
					else {
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setTitle("Functionality limited");
						builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
						builder.setPositiveButton(android.R.string.ok, null);
						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
							}

						});
						builder.show();
					}

				}
			} else {
				if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_BACKGROUND_LOCATION},
							PERMISSION_REQUEST_FINE_LOCATION);
				}
				else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}

			}
		}



        ///HERE ENDS PERMISSION CHECK

        return view;
    }











	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_FINE_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "fine location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
			case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "background location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Functionality limited");
					builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
		}
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        verifyBluetooth();
    }

    public void onRangingClicked(View view) {
        ((BeaconRecorderApplication) getActivity().getApplication()).rangingActivation();
//        Intent myIntent = new Intent(this.getActivity(), RangingService.class);
//        this.getActivity().startService(myIntent);
    }

    public void onEnableClicked(View view) {
        BeaconRecorderApplication application = ((BeaconRecorderApplication) this.getActivity().getApplicationContext());
        if (BeaconManager.getInstanceForApplication(this.getActivity()).getMonitoredRegions().size() > 0) {
            application.disableMonitoring();
                ((Button)getView().findViewById(R.id.enableButton)).setText("Re-Enable Monitoring");
        }
        else {
            ((Button)getView().findViewById(R.id.enableButton)).setText("Disable Monitoring");
            application.enableMonitoring();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconRecorderApplication application = ((BeaconRecorderApplication) this.getActivity().getApplicationContext());
        application.setBeaconTrackerFragment(this);
        updateLog(application.getLog());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconRecorderApplication) this.getActivity().getApplicationContext()).setBeaconTrackerFragment(null);
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this.getActivity()).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();

        }

    }

    public void updateLog(final String log) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)BeaconTrackerFragment.this.getView()
                        .findViewById(R.id.monitoringText);
                editText.setText(log);
            }
        });

    }
    public void updateBeaconView(Beacon firstBeacon){
        final int mRssi = firstBeacon.getRssi();

        getActivity().runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                TextView textView = (TextView)BeaconTrackerFragment.this.getView()
                        .findViewById(R.id.proximity);
                textView.setText(Integer.toString(mRssi));
            }
        });
    }


    }
