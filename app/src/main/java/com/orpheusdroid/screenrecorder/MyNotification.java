package com.orpheusdroid.screenrecorder;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationChannel;


public class MyNotification {

    private static final String TAG = "MyNotification";
    private static String notificationMessage;
    private static NotificationManager notifManager;


    public static void createNotification(Context context, final int progress, final String fName, String aMessage) {
        final int NOTIFY_ID = 0; // ID of notification
        //String id = context.getString(R.string.screen_uploading_notification_action_id); // default_channel_id

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);
        String title = Const.UPLOADING_NOTIFICATION_CHANNEL_NAME; // Default Channel
        Intent intent;
        PendingIntent pendingIntent;
        final NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(Const.UPLOADING_NOTIFICATION_CHANNEL_ID);
            if (mChannel == null) {
                mChannel = new NotificationChannel(Const.UPLOADING_NOTIFICATION_CHANNEL_ID, title, importance);
                mChannel.setVibrationPattern(new long[]{ 0 });
                mChannel.enableVibration(false);
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, Const.UPLOADING_NOTIFICATION_CHANNEL_ID);
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentTitle(aMessage)                            // required
                    .setSmallIcon(R.drawable.ic_notification_upload)   // required
                 //   .setContentText(context.getString(R.string.app_name)) // required
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setDefaults(Notification.DEFAULT_ALL)
                    //.setAutoCancel(true);
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setGroup(Const.GROUP_KEY_WORK_Recorder);

            // Start a lengthy operation in a background thread
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            switch (fName) {
                                case Const.EMPTY_DIRECTORY:
                                    builder.setContentText("All files have been uploaded")
                                            .setOngoing(false)
                                            .setProgress(0, 0, false);
                                    notifManager.notify(NOTIFY_ID, builder.build());
                                    break;

                                case Const.LOOKING_FOR_FILES:
                                    builder.setContentText("File open error!")
                                            //setStyle(new NotificationCompat.BigTextStyle().bigText(Const.FILE_IS_NOT_CLOSED))
                                            .setOngoing(false)
                                            .setProgress(0, 0, false);
                                    notifManager.notify(NOTIFY_ID, builder.build());
                                    break;

                                default:
                                    builder.setContentText(fName)
                                            .setOngoing(true)
                                            .setProgress(100, progress, false);
                                    // Displays the progress bar
                                    notifManager.notify(NOTIFY_ID, builder.build());
                            }
                        }
                    }
            ).start();

        } else {
            builder = new NotificationCompat.Builder(context, Const.UPLOADING_NOTIFICATION_CHANNEL_ID);
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentTitle(aMessage)                            // required
                    .setSmallIcon(R.drawable.ic_notification_upload)   // required
                   // .setContentText(context.getString(R.string.app_name)) // required
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setDefaults(Notification.DEFAULT_ALL)
                    // .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setOnlyAlertOnce(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            //Start a lengthy operation in a background thread
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            switch (fName) {
                                case Const.EMPTY_DIRECTORY:
                                    builder.setContentText("All files have been uploaded")
                                            .setOngoing(false)
                                            .setProgress(0, 0, false);
                                    notifManager.notify(NOTIFY_ID, builder.build());
                                    break;

                                case Const.LOOKING_FOR_FILES:
                                    builder.setContentText("File open error!")
                                            //setStyle(new NotificationCompat.BigTextStyle().bigText(Const.FILE_IS_NOT_CLOSED))
                                            .setOngoing(false)
                                            .setProgress(0, 0, false);
                                    notifManager.notify(NOTIFY_ID, builder.build());
                                    break;

                                default:
                                    builder.setContentText(fName)
                                            .setOngoing(true)
                                            .setProgress(100, progress, false);
                                    // Displays the progress bar
                                    notifManager.notify(NOTIFY_ID, builder.build());
                            }
                        }
                    }
            ).start();
        }

    }

}
