package com.orpheusdroid.screenrecorder;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class UploaderService extends Service {

    private static final String TAG = "MyService";

    static boolean isUploading = false;
    private static Context context;
    private StorageReference storageReference;
    private String androidId;
    private FileObserver fileObserver;
    private List<String> uploadedFiles;
    private File file;
    private static UploadTask uploadTask;
    private Boolean saved = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String stringRef;
    private String fResumedUri;
    private String fResumedName;
    private boolean resume;
    private String fName;
    private Uri fUri;
    private String directoryFirebase;
    private StorageReference fileRef;
    private String fPath;
    private Uri sessionUri;

    private FirebaseAuth mAuth;


    @Override
    public void onCreate() {

        Log.d(TAG, "Try to create the service");

        if (!isUploading) {
            super.onCreate();
            this.context = this;

            androidId = getAndroidID();
            Log.d(TAG, androidId);

            storageReference = FirebaseStorage.getInstance().getReference();

            prefs = getSharedPreferences(Const.PREFS_NAME, Context.MODE_PRIVATE);
            editor = prefs.edit();
            isUploading = false;
            resume = false;

            uploadedFiles = new ArrayList<String>();
            Log.d(TAG, "Service started and running");
        }
    }


    private String getAndroidID() {

        return Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();




        Log.d(TAG, "Service onStartCommand");

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR

        if (!isUploading) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //check if we need to resume after the service gets killed
                        stringRef = prefs.getString(Const.SESSION_KEY, null);
                        fResumedUri = prefs.getString(Const.FILE_URI, null);
                        fResumedName = prefs.getString(Const.FILE_NAME, null);

                        if (isResume()) {
                            resume = true;
                            uploadFiles();
                        } else {
                            Log.d(TAG, "Start uploading...");
                            resume = false;
                            uploadFiles();
                        }

                    } catch (Exception e) {
                    }

                    //Stop service once it finishes its task
                    stopSelf();
                }
            }).start();

        }

        return START_STICKY; // the system will try to re-create your service after it is killed
    }


    public boolean isResume() {
        boolean resume = false;
        prefs = getSharedPreferences(Const.PREFS_NAME, Context.MODE_PRIVATE);
        String sRef = prefs.getString(Const.SESSION_KEY, null);
        String fUri = prefs.getString(Const.FILE_URI, null);
        String fName = prefs.getString(Const.FILE_NAME, null);

        if (sRef != null && fUri != null && fName != null) {
            resume = true;
        }
        return resume;
    }

    private void uploadFiles() {

        if ((MyFiles.getFiles().length) == 0 || MyFiles.getFiles() == null) {
            Log.d(TAG, "No files to be uploaded...");
            MyNotification.showNotification(context, 0, Const.EMPTY_DIRECTORY, true);
            editor.clear();
            editor.commit();
            isUploading = false;
            return; // The directory is empty, loop finishes
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (resume) {
                        file = new File(MyDirectory.path + "/" + fResumedName);
                        fPath = file.getAbsolutePath();
                        fUri = Uri.fromFile(new File(fPath));
                        fName = file.getName();
                        sessionUri = Uri.parse(stringRef);
                        directoryFirebase = androidId.concat("/").concat(fResumedName);
                        fileRef = storageReference.child(directoryFirebase); //"images/pic.jpg" This folder will be created in firebase database, and the file will be created with the specified name
                        uploadTask = fileRef.putFile(fUri,
                                new StorageMetadata.Builder().build(), sessionUri);
                        // resume = true;
                    } else {
                        file = MyFiles.getFileOldest();
                        fPath = file.getAbsolutePath();
                        fUri = Uri.fromFile(new File(fPath));
                        fName = file.getName();
                        directoryFirebase = androidId.concat("/").concat(fName);
                        fileRef = storageReference.child(directoryFirebase);// "images/pic.jpg" This folder will be created in firebase database, and the file will be created with the specified name
                        uploadTask = fileRef.putFile(fUri);
                        // resume = false;
                    }

                    // make sure that the file exits and it is closed
                    if (file != null && file.exists()) {

                        if (MyFiles.isClosed(file)) {
                            // start uploading the file
                            fileObserver = new FileObserver(MyDirectory.path) {
                                @Override
                                public void onEvent(int event, String fileDeleted) {
                                    if (((FileObserver.DELETE | FileObserver.MOVED_FROM) & event) != 0) {
                                        if (fName.equals(fileDeleted)) {
                                            Log.d(TAG, "We are in the observer");
                                            Log.d(TAG, "FileObserver: " + event + " " + "filePath: " + fileDeleted);
                                            uploadTask.pause();// if the file gets deleted during the upload process, then the upload process should stop
                                            MyNotification.showNotification(context, 0, Const.EMPTY_DIRECTORY, true);
                                            isUploading = false;
                                            editor.clear();
                                            editor.commit();
                                            uploadFiles();
                                        } else {
                                            Log.d(TAG, "Other file was deleted " + fileDeleted);
                                        }
                                    }
                                }
                            };
                            fileObserver.startWatching();

                            //TODO check if the file has not been stored before
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d(TAG, "uploaded successfully");
                                    uploadedFiles.add(fName);
                                    Log.d(TAG + " Size: ", String.valueOf(uploadedFiles.size()));
                                    MyFiles.deleteFile(fUri);
                                    editor.clear();
                                    editor.commit();
                                    isUploading = false;
                                    resume = false;
                                    uploadFiles(); // loop continues only if you get the success callback from previous request.
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception exception) {
                                            int errorCode = ((StorageException) exception).getErrorCode();
                                            uploadTask.pause();
                                            String errorMessage = exception.getMessage();
                                            Log.d(TAG, "error msg: " + errorMessage + errorCode);
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            Uri sessionUri = taskSnapshot.getUploadSessionUri();
                                            isUploading = true;
                                            if (sessionUri != null && !saved) {
                                                saved = true;
                                                Log.d(TAG, sessionUri.toString());
                                                editor.putString(Const.SESSION_KEY, sessionUri.toString());
                                                editor.putString(Const.FILE_URI, fUri.toString());
                                                editor.putString(Const.FILE_NAME, fName);
                                                editor.commit();
                                            }

                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            //    Log.d(TAG, "Progress " + progress + " " + fName);

                                            if (resume) {
                                                Log.d(TAG, "Resume:" + progress + " " + fName);
                                                MyNotification.showNotification(context, progress, fName, false);
                                            } else {
                                                Log.d(TAG, "Upload: " + progress + " " + fName);
                                                MyNotification.showNotification(context, progress, fName, true);
                                            }

                                            if (!Power.isCharging(context) || !NetworkUtil.isConnected(context)) {
                                                isUploading = false;
                                                resume = false;
                                                uploadTask.pause();
                                            }
                                        }
                                    });
                        } else {
                            MyNotification.showNotification(context, 0, "LookingForFile", true);
                        }

                    } else {
                        isUploading = false;
                        resume = false;
                        Log.d(TAG, "The file does not exist...");
                        //make sure that the shared preferences is empty now
                        editor.clear();
                        editor.commit();
                        uploadFiles();
                    } // The file does not exist
                } catch (Exception e) {
                    isUploading = false;
                    resume = false;
                    throw e;
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



