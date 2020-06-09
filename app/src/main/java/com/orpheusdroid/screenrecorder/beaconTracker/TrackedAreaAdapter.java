package com.orpheusdroid.screenrecorder.beaconTracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.googlecode.mp4parser.authoring.Track;
import com.orpheusdroid.screenrecorder.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class TrackedAreaAdapter extends RecyclerView.Adapter<TrackedAreaAdapter.ViewHolder> {

    private ArrayList<TrackedArea> mTrackedAreas;
    private ArrayList<TrackedBeacon> trackedBeacons;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int lastClickedItem = -1;;
    private Context c;

    // data is passed into the constructor
    TrackedAreaAdapter(Context context, ArrayList<TrackedArea> trackedAreas, ArrayList<TrackedBeacon> trackedBeacons) {
        this.c = context;
        this.mInflater = LayoutInflater.from(context);
        this.mTrackedAreas = trackedAreas;
        this.trackedBeacons = trackedBeacons;
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


         updateBeaconLight(area,beacon1,holder.beaconButton1text,holder.beaconRow1,holder.statusB1);
         updateBeaconLight(area,beacon2,holder.beaconButton2text,holder.beaconRow2,holder.statusB2);
         updateBeaconLight(area,beacon3,holder.beaconButton3text,holder.beaconRow3,holder.statusB3);

         if(beacon2!=null){
             holder.beaconButton2text.setText(area.getObjectByIndex(1).getDescription());
             holder.beaconRow2.setVisibility(View.VISIBLE);
         }else{holder.beaconRow2.setVisibility(View.INVISIBLE);}

         if(beacon3!=null){
             holder.beaconButton3text.setText(area.getObjectByIndex(2).getDescription());
             holder.beaconRow3.setVisibility(View.VISIBLE);
         }else{holder.beaconRow3.setVisibility(View.INVISIBLE);}


         holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastClickedItem = position;
                showAlertChoice(view.getContext());
            }  });
         holder.sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastClickedItem = position;
                syncBeacons();
            }  });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mTrackedAreas.size();
    }

    public void updateBeaconLight(TrackedArea area, TrackedBeacon tb, TextView tv, LinearLayoutCompat br, Button s){
         //This section are a lot of repetition, can be solved more clear - but functionality is:
        //by getting the objects first 3 beacons and shows them that are not null
         if(tb!=null){
             tv.setText(area.getObjectByIndex(0).getDescription());
             br.setVisibility(View.VISIBLE);
             Log.d("what",tb.getDescription() + " "+tb.whenLastSeen());
             if(tb.whenLastSeen()>10000) {
                 s.setBackgroundResource(R.drawable.beacon_lost);
                 return;
             } else {
                 if (tb.getProximity() < area.getThresholdDistance(tb)) {
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

            beaconButton1text = itemView.findViewById(R.id.beacon1text);;
            beaconButton2text = itemView.findViewById(R.id.beacon2text);;
            beaconButton3text = itemView.findViewById(R.id.beacon3text);;
            plus = itemView.findViewById(R.id.addNewBeaconToArea);
            sync = itemView.findViewById(R.id.syncBeaconsInArea);
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
}
