package com.orpheusdroid.screenrecorder.beaconTracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.orpheusdroid.screenrecorder.R;

import java.util.ArrayList;

public class TrackedBeaconAdapter extends RecyclerView.Adapter<TrackedBeaconAdapter.ViewHolder> {

    private ArrayList<TrackedBeacon> mTrackedBeacons;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int lastClickedItem = -1;;
    private Context c;

    // data is passed into the constructor
    TrackedBeaconAdapter(Context context, ArrayList<TrackedBeacon> trackedBeacons) {
        this.c = context;
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TrackedBeacon beaconAtPos = mTrackedBeacons.get(position);
        holder.beaconName.setText(beaconAtPos.getDescription());

        holder.beaconUuid.setText("UUID: " + beaconAtPos.getUuid());
        holder.beaconMajor.setText("Major: "+beaconAtPos.getMajor());
        holder.beaconMinor.setText("Minor: "+beaconAtPos.getMinor());
        holder.beaconProximity.setText(""+String.format("%.2f",beaconAtPos.getProximity()));
        if(beaconAtPos.lastSeen != null){
            holder.bluetoothImage.getBackground().setAlpha((int) Math.floor(255*(1f-(float)beaconAtPos.whenLastSeen()/10000)));
        }else{holder.bluetoothImage.getBackground().setAlpha(0);}
        holder.beaconName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastClickedItem = position;
                setNameofBeacon(view);
            }  });

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
        ImageView bluetoothImage;


        ViewHolder(View itemView) {
            super(itemView);
            beaconName = itemView.findViewById(R.id.rowbeaconName);
            beaconUuid = itemView.findViewById(R.id.rowuuid);
            beaconMinor = itemView.findViewById(R.id.rowminor);
            beaconMajor = itemView.findViewById(R.id.rowmajor);
            beaconProximity = itemView.findViewById(R.id.rowProximity);
            bluetoothImage = itemView.findViewById(R.id.rowBeaconImage);
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

    public void setNameofBeacon(View view) {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Name");

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

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private void sendDialogDataToActivity(String data) {
        if(!(data.isEmpty())){
            mTrackedBeacons.get(lastClickedItem).setDescription(data);
        }

        this.notifyDataSetChanged();
    }
}