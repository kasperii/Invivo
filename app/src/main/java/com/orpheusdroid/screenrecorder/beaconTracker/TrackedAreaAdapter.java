package com.orpheusdroid.screenrecorder.beaconTracker;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orpheusdroid.screenrecorder.R;

import java.util.ArrayList;

public class TrackedAreaAdapter extends RecyclerView.Adapter<TrackedBeaconAdapter.ViewHolder> {

    private ArrayList<TrackedArea> mTrackedAreas;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    TrackedAreaAdapter(Context context, ArrayList<TrackedArea> trackedAreas) {
        this.mInflater = LayoutInflater.from(context);
        this.mTrackedAreas = trackedAreas;
    }
    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.area_row, parent, false);
        return new ViewHolder(view);
    }



    // binds the data to the views
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrackedArea area = mTrackedAreas.get(position);
        holder.beaconName.setText("Area " + position);
//        holder.beaconUuid.setText(beaconAtPos.getUuid());
//        holder.beaconMajor.setText(beaconAtPos.getMajor());
//        holder.beaconMinor.setText(beaconAtPos.getMinor());
//        holder.beaconProximity.setText(""+beaconAtPos.getProximity());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mTrackedAreas.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView beaconName;
//        TextView beaconUuid;
//        TextView beaconMinor;
//        TextView beaconMajor;
//        TextView beaconProximity;


        ViewHolder(View itemView) {
            super(itemView);
//            beaconName = itemView.findViewById(R.id.rowbeaconName);
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
}
