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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.altbeacon.beacon.BeaconManager;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by vijai on 11-10-2016.
 */

public class BeaconTrackerFragment extends Fragment{

    private static final String TAG = "BeaconReferenceFragment";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private TrackedBeaconAdapter trackedBeaconAdapter;
    private TrackedAreaAdapter trackedAreaAdapter;
    private SearchBeaconAdapter searchBeaconAdapter;
    private BeaconRecorderApplication myApp;
    View view;
    Context c;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.c = context;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myApp = (BeaconRecorderApplication)getActivity().getApplication();
        view = inflater.inflate(R.layout.fragment_beacons, container, false);


        Button enableButton = (Button) view.findViewById(R.id.monitoringSwitch);
        enableButton.setOnClickListener(new View.OnClickListener()
        {
             @Override
             public void onClick(View v)
             {
                 onEnableClicked(v);
             }
        });


        // set up the RecyclerView for tracked beacons
        RecyclerView recyclerViewBeacon = view.findViewById(R.id.list_of_Beacons);
        recyclerViewBeacon.setLayoutManager(new LinearLayoutManager(getActivity()));

        trackedBeaconAdapter = new TrackedBeaconAdapter(getContext(), myApp.getTrackedBeacons(), myApp);
        //trackedBeaconAdapter.setClickListener(getActivity());
        recyclerViewBeacon.setAdapter(trackedBeaconAdapter);


        // set up the RecyclerView for tracked areas
        RecyclerView recyclerViewArea = view.findViewById(R.id.list_of_Areas);
        recyclerViewArea.setLayoutManager(new LinearLayoutManager(getActivity()));
        trackedAreaAdapter = new TrackedAreaAdapter(getContext(), myApp.getTrackedAreas(), myApp.getTrackedBeacons(), myApp);
        recyclerViewArea.setAdapter(trackedAreaAdapter);




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
    public void onDestroyView() {
        super.onDestroyView();
        view = null; // now cleaning up!
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


    public void onEnableClicked(View view) {
        BeaconRecorderApplication application = ((BeaconRecorderApplication) this.getActivity().getApplicationContext());
        if (BeaconManager.getInstanceForApplication(this.getActivity()).getMonitoredRegions().size() > 0) {
            application.disableMonitoring();
            getView().setAlpha(0.4f);
            //((Button)getView().findViewById(R.id.monitoringSwitch));
        }
        else {
            //((Button)getView().findViewById(R.id.monitoringSwitch)).setText("Disable Monitoring");
            application.enableMonitoring();
            getView().setAlpha(1f);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconRecorderApplication application = ((BeaconRecorderApplication) this.getActivity().getApplicationContext());
        application.setBeaconTrackerFragment(this);
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

    public void updateBeaconView(){
        trackedBeaconAdapter.notifyDataSetChanged();
        trackedAreaAdapter.notifyDataSetChanged();
    }

    public void updateSearchBeaconView(){
        searchBeaconAdapter.notifyDataSetChanged();
    }

    public void addNewObject(){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_beacon_area_selection, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        final Button areaButton = (Button) popupView.findViewById(R.id.addAreaButton);
        areaButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              popupWindow.dismiss();
                                              addNewArea();
                                              return;
                                          }
                                      });
        final Button beaconButton = (Button) popupView.findViewById(R.id.addBeaconButton);
        beaconButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              popupWindow.dismiss();
                                              searchNewBeacon();
                                              return;
                                          }
                                      });
//        @Override
//          public boolean onClick(View v, MotionEvent event) {
//              popupWindow.dismiss();
//              return true;
//          }

        // dismiss the popup window when touched

    }
    public void addNewItem(){
        addNewObject();
    }

    public void searchNewBeacon(){



        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_beacon_search_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken

        // set up the RecyclerView for tracked beacons
        RecyclerView recyclerViewSearchBeacon = popupView.findViewById(R.id.list_of_searchingBeacons);
        recyclerViewSearchBeacon.setLayoutManager(new LinearLayoutManager(myApp.getApplicationContext()));

        searchBeaconAdapter = new SearchBeaconAdapter(this.c, myApp.getFoundBeacons(), myApp.getTrackedBeacons());
        //trackedBeaconAdapter.setClickListener(getActivity());
        recyclerViewSearchBeacon.setAdapter(searchBeaconAdapter);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        myApp.setSearchingForNewBeacons(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                myApp.setSearchingForNewBeacons(false);
            }
        });
//        @Override
//          public boolean onClick(View v, MotionEvent event) {
//              popupWindow.dismiss();
//              return true;
//          }

        // dismiss the popup window when touched

    }
    public void addNewArea(){
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Set a name for the new area");

        // set the custom layout
        final View customLayout = inflater.inflate(R.layout.change_text, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText editText = customLayout.findViewById(R.id.editText);
                String name = editText.getText().toString();
                if(myApp.isNoOtherAreaBySameName(name)){
                    myApp.createNewArea(name);
                }else{
                    myApp.makeToastHere("There is already an area with that name");
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
