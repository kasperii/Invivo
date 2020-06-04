package com.orpheusdroid.screenrecorder.beaconTracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.orpheusdroid.screenrecorder.R;

import java.util.ArrayList;

public class TrackedBeaconAdapter extends RecyclerView.Adapter<TrackedBeaconAdapter.ViewHolder> {

    private ArrayList<TrackedBeacon> mTrackedBeacons;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    TrackedBeaconAdapter(Context context, ArrayList<TrackedBeacon> trackedBeacons) {
        this.mInflater = LayoutInflater.from(context);
        this.mTrackedBeacons = trackedBeacons;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.beacon_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the views
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrackedBeacon beaconAtPos = mTrackedBeacons.get(position);
        holder.beaconName.setText("Beacon " + position);
        holder.beaconUuid.setText("UUID: " + beaconAtPos.getUuid());
        holder.beaconMajor.setText("Major: "+beaconAtPos.getMajor());
        holder.beaconMinor.setText("Minor: "+beaconAtPos.getMinor());
        holder.beaconProximity.setText(""+String.format("%.2f",beaconAtPos.getProximity()));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mTrackedBeacons.size();
    }


    // stores and recycles views as they are scrolled off screen

    public class ViewHolder extends RecyclerView.ViewHolder { // implements View.OnClickListener
        TextView beaconName;
        TextView beaconUuid;
        TextView beaconMinor;
        TextView beaconMajor;
        TextView beaconProximity;


        ViewHolder(View itemView) {
            super(itemView);
            beaconName = itemView.findViewById(R.id.rowbeaconName);
            beaconUuid = itemView.findViewById(R.id.rowuuid);
            beaconMinor = itemView.findViewById(R.id.rowminor);
            beaconMajor = itemView.findViewById(R.id.rowmajor);
            beaconProximity = itemView.findViewById(R.id.rowProximity);
            //itemView.setOnClickListener(this);
        }

//        @Override
//        public void onClick(View view) {
//            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
//        }
    }

    // convenience method for getting data at click position
    TrackedBeacon getItem(int id) {
        return mTrackedBeacons.get(id);
    }

//    // allows clicks events to be caught
//    void setClickListener(ItemClickListener itemClickListener) {
//        this.mClickListener = itemClickListener;
//    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}