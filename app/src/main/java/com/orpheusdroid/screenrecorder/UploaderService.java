package com.orpheusdroid.screenrecorder;



import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.os.IBinder;
import android.provider.Settings;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;



import io.tus.android.client.TusPreferencesURLStore;
import io.tus.java.client.TusClient;

import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;

public class UploaderService extends Service {

    private static final String TAG = "MyService";

    private boolean isUploading;
    private boolean isCharging;
    private boolean isConnected;
    private boolean isPaused;
    private static Context context;
    private String androidId;
    private File file;
    private String fileName;
    private String filePath;
    private Uri fileUri;

    private TusClient client;
    private UploadTask uploadTask;
    private NotificationManager mNotificationManager;
    private FloatingControlService floatingControlService;
    private boolean isBound = false;
    private URL uploaderURL;

    @Override
    public void onCreate() {

        if (!isUploading) {
            super.onCreate();
            this.context = this;
            androidId = getAndroidID();
            Log.d(TAG, androidId);
            Log.d(TAG, "Service started and running");
        }

        // Create a new TusClient
        client = new TusClient();
        // Configure tus HTTP endpoint. This URL will be used for creating new uploads
        // using the Creation extension
        try {
            client.setUploadCreationURL(new URL("https://invivo.dsv.su.se/files/"));
            // Enable resuming uploads by sorting the upload URL in the preferences
            // and preserve them after app restarts
            SharedPreferences pref = getSharedPreferences("tus", 0);
            client.enableResuming(new TusPreferencesURLStore(pref));
        } catch (MalformedURLException e) {
            Log.d(TAG, e.toString());
        }
    }


    private String getAndroidID() {

        return Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service onStartCommand");

        switch (intent.getAction()) {
            case Const.FILE_UPLOADING_START:
                Log.d(TAG, "Start uploading");
                isCharging = true;
                isConnected = true;
                isPaused = false;
                beginUpload();
                break;

            case Const.FILE_UPLOADING_STOP:
                Log.d(TAG, "Stop uploading");
                isCharging = false;
                isConnected = false;
                break;

        }

        return START_STICKY;
    }

    private void beginUpload() {
        resumeUpload();
    }


    //Service connection to manage the connection state between this service and the bounded service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Get the service instance
            FloatingControlService.ServiceBinder binder = (FloatingControlService.ServiceBinder) service;
            floatingControlService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            floatingControlService = null;
            isBound = false;
        }
    };

    public void pauseUpload() {
        uploadTask.cancel(false);
    }


    public void resumeUpload() {

        Log.d(TAG, "Upload and resume");
        try {
            if ((MyFiles.getFiles(MyDirectory.path).size()) == 0 || MyFiles.getFiles(MyDirectory.path) == null) {
                Log.d(TAG, "No files to be uploaded...");
                MyNotification.createNotification(context, 0, Const.EMPTY_DIRECTORY, Const.NO_FILE_TO_BE_UPLOADED);
                return;
            }

            file = MyFiles.getFileOldest(MyDirectory.path);
            filePath = file.getAbsolutePath();
            fileName = file.getName();
            fileUri = Uri.fromFile(new File(filePath));
            TusUpload upload = new TusUpload(file);

            Log.d(TAG, String.valueOf(MyFiles.getFileLength(MyDirectory.path)));

            // make sure that the file exits and it is closed
            if (file != null && file.exists()) {
                Log.d(TAG, "Closed: "+String.valueOf(MyFiles.isClosed(file)));
                Log.d(TAG, fileName);
                if (MyFiles.isClosed(file)) {
                    Log.d(TAG, "File length: "+file.length());
                    uploadTask = new UploadTask(this, client, upload);
                    uploadTask.execute(new Void[0]);
                } else {
                    MyNotification.createNotification(context, 0, Const.LOOKING_FOR_FILES, Const.WAITING_FILES_CLOSE);
                }
            } else {
                beginUpload();
            }

        } catch (Exception e) {
            Log.d(TAG + "uploading tus ", e.toString());
            Log.d(TAG, "Not uploading" );

        }
    }


    private class UploadTask extends AsyncTask<Void, Long, URL> {
        private TusClient client;
        private TusUpload upload;
        private Exception exception;

        public UploadTask(UploaderService service, TusClient client, TusUpload upload) {
            this.client = client;
            this.upload = upload;
        }

        @Override
        protected void onPreExecute() {
            //activity.setPauseButtonEnabled(true);
        }

        @Override
        protected void onPostExecute(URL uploadURL) {
            // delete file
            Log.d(TAG, "Upload finished.");
            Log.d(TAG, "Upload available at:" + uploadURL.toString());
            MyFiles.deleteFile(fileUri, context);
            isUploading = false;
            isPaused = false;
            uploadJson(fileName.substring(0,fileName.length()- 4));
            beginUpload();
        }

        @Override
        protected void onCancelled() {
            if (exception != null) {
                Log.d(TAG, exception.toString());
            }
            // activity.setPauseButtonEnabled(false);
        }


        @Override
        protected void onProgressUpdate(Long... updates) {
            long uploadedBytes = updates[0];
            long totalBytes = updates[1];
            final int progress = (int) ((double) uploadedBytes / totalBytes * 100);

            if (isPaused) {
                return;
            }

            // start uploading the file
            FileObserver fileObserver = new FileObserver(MyDirectory.path) {
                @Override
                public void onEvent(int event, String fileDeleted) {
                    if (((FileObserver.DELETE | FileObserver.MOVED_FROM) & event) != 0) {
                        if (fileName.equals(fileDeleted) && progress != 100) {
                            Log.d(TAG, "We are in the observer");
                            Log.d(TAG, "FileObserver: " + event + " " + "filePath: " + fileDeleted);
                            MyNotification.createNotification(context, 0, fileName, Const.FILE_IS_DELETED);
                            pauseUpload(); // if the file gets deleted during the upload process, then the upload process should stop
                            isPaused = true;
                            beginUpload();
                        } else {
                            Log.d(TAG, "Other file was deleted " + fileDeleted);
                        }
                    }
                }
            };
            fileObserver.startWatching();

            Log.d(TAG, "Upload: " + progress + " " + fileName);
            MyNotification.createNotification(context, progress, fileName, Const.UPLOAD);
            if (!isCharging || !isConnected) {
                MyNotification.createNotification(context, progress, fileName, Const.NO_CHARGER);
                isPaused = true;
                pauseUpload();
            }
        }

        @Override
        protected URL doInBackground(Void... params) {
            try {
                TusUploader uploader = client.resumeOrCreateUpload(upload);
                long totalBytes = upload.getSize();
                long uploadedBytes = uploader.getOffset();
                uploaderURL =  uploader.getUploadURL();

                Log.d(TAG, "Total bytes"+ totalBytes);

                // Upload file in 1MiB chunks
                uploader.setChunkSize(1024 * 1024);

                Log.d(TAG, "before while");
                Log.d(TAG, "before while"+ isCancelled());
                Log.d(TAG, "before while"+ uploader.uploadChunk());


                while (!isCancelled() && uploader.uploadChunk() > 0) {
                    Log.d(TAG, "While"+ isCancelled());
                    Log.d(TAG, "While"+ uploader.uploadChunk());
                    uploadedBytes = uploader.getOffset();
                    publishProgress(uploadedBytes, totalBytes);
                }

                Log.d(TAG, "after while");

                uploader.finish();
                return uploader.getUploadURL();

            } catch (Exception e) {
                exception = e;
                Log.d(TAG, "doInBackground exception");
                Log.d(TAG, "Upload URL: "+uploaderURL.toString());
                Log.d(TAG, String.valueOf(e));
                cancel(true);
            }
            return null;
        }
    }


    public void uploadJson(final String jsonFileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream os = null;
                InputStream is = null;
                HttpURLConnection conn = null;
                try {
                    //constants
                    URL url = new URL("https://invivo.dsv.su.se/upload.php");
                    Log.d(TAG, "Json upload");
                    File file = new File(MyDirectory.path + File.separator + jsonFileName + ".json");

                    FileInputStream stream = new FileInputStream(file);
                    String jString = null;
                    try {
                        FileChannel fc = stream.getChannel();
                        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                        /* Instead of using default, pass in a decoder. */
                        jString = Charset.defaultCharset().decode(bb).toString();
                    }catch(Exception e){
                        Log.d(TAG, "Exception file inputstream: "+e);
                    }
                    finally {
                        stream.close();
                    }

                    JSONObject jsonObj = new JSONObject(jString);
                    jsonObj.put("localPath", uploaderURL);
                    jString = jsonObj.toString();
                    Log.d(TAG, "JString "+jString);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /*milliseconds*/);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setFixedLengthStreamingMode(jString.getBytes().length);

                    //make some HTTP header nicety
                    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                    //open
                    conn.connect();

                    //setup send
                    os = new BufferedOutputStream(conn.getOutputStream());
                    os.write(jString.getBytes());
                    //clean up
                    os.flush();

                    //do somehting with response
                    is = conn.getInputStream();
                    String contentAsString = is.toString();
                    Log.d(TAG, "success");
                    Log.d(TAG, "Content as String: "+contentAsString);
                    MyFiles.deleteFile(Uri.fromFile(file), context);

                } catch (Exception e) {
                    Log.d(TAG, "ERROR JSON" +e);
                    e.printStackTrace();
                } finally {
                    //clean up
                    try {
                        if (os != null && is != null){
                            os.close();
                            is.close();
                        }
                        conn.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isUploading = false;
        Log.d(TAG, "Service destroyed and stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

}



