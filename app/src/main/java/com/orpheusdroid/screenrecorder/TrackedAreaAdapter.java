package com.orpheusdroid.screenrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class TrackedAreaAdapter extends RecyclerView.Adapter<TrackedAreaAdapter.ViewHolder> {

    private DataInformationChangeListener listener;
    private ArrayList<TrackedArea> mTrackedAreas;
    private ArrayList<TrackedBeacon> trackedBeacons;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int lastClickedItem = -1;;
    private Context c;
    private SharedPreferences statePrefs;

    // data is passed into the constructor
    TrackedAreaAdapter(Context context, ArrayList<TrackedArea> trackedAreas, ArrayList<TrackedBeacon> trackedBeacons, DataInformationChangeListener listener) {
        this.c = context;
        this.listener = listener;
        this.mInflater = LayoutInflater.from(context);
        this.mTrackedAreas = trackedAreas;
        this.trackedBeacons = trackedBeacons;
        statePrefs = c.getSharedPreferences("Beacon", Context.MODE_PRIVATE);
    }
    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.area_row, parent, false);
        return new ViewHolder(view);
    }


    // binds the data to the views


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TrackedArea area = mTrackedAreas.get(position);
        holder.areaName.setText("" + area.getName());

        TrackedBeacon beacon1 = area.getObjectByIndex(0);
        TrackedBeacon beacon2 = area.getObjectByIndex(1);
        TrackedBeacon beacon3 = area.getObjectByIndex(2);
        //This section are a lot of repetition, can be solved more clear - but functionality is:
        //by getting the objects first 3 beacons and shows them that are not null


        updateBeaconLight(area, beacon1, holder.beaconButton1text, holder.beaconRow1, holder.statusB1);
        updateBeaconLight(area, beacon2, holder.beaconButton2text, holder.beaconRow2, holder.statusB2);
        updateBeaconLight(area, beacon3, holder.beaconButton3text, holder.beaconRow3, holder.statusB3);

        if (beacon2 != null) {
            holder.beaconButton2text.setText(area.getObjectByIndex(1).getDescription());
            holder.beaconRow2.setVisibility(View.VISIBLE);
        } else {
            holder.beaconRow2.setVisibility(View.INVISIBLE);
        }

        if (beacon3 != null) {
            holder.beaconButton3text.setText(area.getObjectByIndex(2).getDescription());
            holder.beaconRow3.setVisibility(View.VISIBLE);
        } else {
            holder.beaconRow3.setVisibility(View.INVISIBLE);
        }

        if(!(mTrackedAreas.get(position).isRecordingActive)){holder.itemView.setAlpha(0.4f);}else{holder.itemView.setAlpha(1f);}


        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                lastClickedItem = position;
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.inflate(R.menu.tracked_area_menu);
                Menu menuOpts = popup.getMenu();
                if(mTrackedAreas.get(lastClickedItem).isRecordingActive){
                    menuOpts.getItem(4).setTitle("Dectivate");
                }else{menuOpts.getItem(4).setTitle("Reactivate");}
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.rename_item:
                                setNameofArea(view);
                                return true;
                            case R.id.remove_item:
                                areYouSureDelete();
                                return true;
                            case R.id.sync_item:
                                syncBeacons();
                                return true;
                            case R.id.add_beacon_item:
                                chooseBeaconsInArea(view);
                                return true;
                            case R.id.activate_item:
                                if(mTrackedAreas.get(lastClickedItem).isRecordingActive){
                                    mTrackedAreas.get(lastClickedItem).setActivated(false);
                                }else{
                                    mTrackedAreas.get(lastClickedItem).setActivated(true);
                                }
                                notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
        //displaying the popup
        popup.show();

            }
        });
        syncBeaconAreaToShared();
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mTrackedAreas.size();
    }

    public void updateBeaconLight(TrackedArea area, TrackedBeacon tb, TextView tv, LinearLayoutCompat br, Button s){
         //In this section there are a lot of repetition, can be solved more clear - but functionality is:
        //by getting the objects first 3 beacons and shows them that are not null
         if(tb!=null){
             tv.setText(area.getObjectByIndex(0).getDescription());
             br.setVisibility(View.VISIBLE);
             Log.d("updateBeaconLight",tb.getDescription() + " "+tb.whenLastSeen());
             if(tb.whenLastSeen()>10000) {
                 s.setBackgroundResource(R.drawable.beacon_lost);
                 return;
             } else {
                 if (tb.getProximity() > area.getThresholdDistance(tb)) {
                     s.setBackgroundResource(R.drawable.beacon_inside);
                     return;
                 } else {
                     s.setBackgroundResource(R.drawable.beacon_found);
                 }
        }

         }else{br.setVisibility(View.INVISIBLE);}

    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView areaName;
        LinearLayoutCompat beaconRow1;
        LinearLayoutCompat beaconRow2;
        LinearLayoutCompat beaconRow3;
        Button statusB1;
        Button statusB2;
        Button statusB3;
        TextView beaconButton1text;
        TextView beaconButton2text;
        TextView beaconButton3text;
        Button plus;
        Button sync;
        Button moreButton;

//        TextView beaconUuid;
//        TextView beaconMinor;
//        TextView beaconMajor;
//        TextView beaconProximity;


        ViewHolder(View itemView) {
            super(itemView);
            areaName = itemView.findViewById(R.id.rowAreaName);
            beaconRow1 = itemView.findViewById(R.id.beacon1);
            beaconRow2 = itemView.findViewById(R.id.beacon2);
            beaconRow3 = itemView.findViewById(R.id.beacon3);
            statusB1 = itemView.findViewById(R.id.statusBeacon1);
            statusB2 = itemView.findViewById(R.id.statusBeacon2);
            statusB3 = itemView.findViewById(R.id.statusBeacon3);

            beaconButton1text = itemView.findViewById(R.id.beacon1text);
            beaconButton2text = itemView.findViewById(R.id.beacon2text);
            beaconButton3text = itemView.findViewById(R.id.beacon3text);
            moreButton = itemView.findViewById(R.id.area_menu_button);
//            beaconUuid = itemView.findViewById(R.id.rowuuid);
//            beaconMinor = itemView.findViewById(R.id.rowminor);
//            beaconMajor = itemView.findViewById(R.id.rowmajor);
//            beaconProximity = itemView.findViewById(R.id.rowProximity);
            //itemView.setOnClickListener(this);
        }

//        @Override
//        public void onClick(View view) {
//            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
//        }
    }

    // convenience method for getting data at click position
    TrackedArea getItem(int id) {
        return mTrackedAreas.get(id);
    }

//    // allows clicks events to be caught
//    void setClickListener(ItemClickListener itemClickListener) {
//        this.mClickListener = itemClickListener;
//    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void showAlertChoice(Context c){
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Select the beacons for the area");

        // add a checkbox list
        if(trackedBeacons==null) {return;}
        int i = 0;
        List<String> beaconString = new ArrayList<>(trackedBeacons.size());
        final boolean[] checkedItems = new boolean[trackedBeacons.size()];
        for (TrackedBeacon b : trackedBeacons) {
            beaconString.add(b.getDescription());
            checkedItems[i] = false;
            i++;
        }
        String[]beaconStringArray= beaconString.toArray(new String[0]);


        //boolean[] checkedItems = {true, false, false, true, false};

        builder.setMultiChoiceItems(beaconStringArray,checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            // user checked or unchecked a box
            checkedItems[which] = isChecked;
        }
        });


        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                TrackedArea area =  mTrackedAreas.get(lastClickedItem);
                for(int val=0;val<trackedBeacons.size();val++){
                    if(checkedItems[val]){
                        //get the tracked beacon of val val;
                        area.addBeaconThreshold(trackedBeacons.get(val),area.getThresholdDistance(trackedBeacons.get(val)));
                    }else{
                        area.removeBeaconThreshold(trackedBeacons.get(val));
                    }
                }
                notifyDataSetChanged();
                listener.resetDataCollection();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void setProximityThreshold(Context c){
        //
    }



    //this method sets the proximity based on the current location
    public void syncBeacons(){
        String text = "Syncing beacon proximity to ";
        TrackedArea area =  mTrackedAreas.get(lastClickedItem);
        text += String.format("%.2f", area.setProximityByIndex(0));
        text += " " + String.format("%.2f", area.setProximityByIndex(1));
        text += " " + String.format("%.2f", area.setProximityByIndex(2));

        Toast toast = Toast.makeText(this.c, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void saveObjectInShared(ArrayList myListOfObjects, String s){

        SharedPreferences.Editor myEdit = statePrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myListOfObjects);
        myEdit.putString(s, json);
        myEdit.commit();
    }

    public void syncBeaconAreaToShared(){
        saveObjectInShared(mTrackedAreas,"trackedAreas");
        saveObjectInShared(trackedBeacons,"trackedBeacons");
    }

    private void chooseBeaconsInArea(View view){
        showAlertChoice(view.getContext());
    }

    private void areYouSureDelete(){

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        final TrackedArea area =  mTrackedAreas.get(lastClickedItem);
        builder.setCancelable(true);
            builder.setTitle("Deleting area");
            builder.setMessage("Are you sure you want to delete " + area.getName());
            builder.setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mTrackedAreas.remove(area);
                            notifyItemRemoved(lastClickedItem);
                            notifyItemRangeChanged(lastClickedItem,mTrackedAreas.size());
                            listener.resetDataCollection();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
    }
    public void setNameofArea(View view) {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Rename the area");

        // set the custom layout
        final View customLayout = mInflater.inflate(R.layout.change_text, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText editText = customLayout.findViewById(R.id.editText);
                sendDialogDataToActivity(editText.getText().toString());
            }
        });


        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendDialogDataToActivity(String data) {
    if(!(data.isEmpty())){
        if(data.equalsIgnoreCase(mTrackedAreas.get(lastClickedItem).getName())) {
            mTrackedAreas.get(lastClickedItem).setName(data);
        }
    }
        this.notifyDataSetChanged();
        listener.resetDataCollection();
    }
}
