package com.orpheusdroid.screenrecorder;


import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;



public class MyNotification {

    private static final String TAG = "MyNotification";
    private static int uploadProgress;

    public static void showNotification(Context context, final double progress, final String fName, boolean upload) {
        final int id = 1;
        final String msg;
        final NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, fName);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);

        mBuilder.setLargeIcon(
                Bitmap.createScaledBitmap(icon, 128, 128, false));
        if (upload) {
            mBuilder.setContentTitle("Files uploading")
                    .setSmallIcon(R.drawable.ic_notification_upload);
        } else {

            mBuilder.setContentTitle("Files resuming")
                    .setSmallIcon(R.drawable.ic_notification_upload);
        }

        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        uploadProgress = (int) progress;

                        if (fName.equals(Const.EMPTY_DIRECTORY)) {
                            mBuilder.setContentText("No files to be uploaded")
                                    .setProgress(0, 0, false);
                            mNotifyManager.notify(id, mBuilder.build());
                        } else if (fName.equals("LookingForFile")) {
                            mBuilder.setContentText("Waiting for the file to close")
                                    .setProgress(0, 0, false);
                            mNotifyManager.notify(id, mBuilder.build());
                        } else {
                            mBuilder.setContentText(fName)
                                    .setProgress(100, uploadProgress, false);
                            // Displays the progress bar
                            mNotifyManager.notify(id, mBuilder.build());
                        }
                    }
                }
        ).start();
    }

}
