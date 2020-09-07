package com.orpheusdroid.screenrecorder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MyFiles {

    public static final String TAG = "MyFiles";
    private static ArrayList<File> fList;
    private static File directory = new File(MyDirectory.path);

    public static ArrayList<File> getFiles(String myDirectory) {
        File[] fList = directory.listFiles();
        ArrayList<File> allFiles = new ArrayList<>();

        for (File file : fList) {
            if (file.isFile() && (file.toString().endsWith(".mp4") || file.toString().endsWith(".json"))) {
                allFiles.add(file);
            } else if (file.isDirectory()) {
                getFiles(file.getAbsolutePath());
            }
        }

//
//
//        int i = 0;
//        for (File file : fList) {
//            if (file.isFile() && file.toString().endsWith(".mp4")) {
//                i++;
//            } else if (file.isDirectory()) {
//                getFiles(file.getAbsolutePath());
//            }
//        }
//
//        File[] files = new File[i];
//
//        i = 0;
//
//        for (File file : fList) {
//            if (file.isFile() && file.toString().endsWith(".mp4")) {
//                files[i] = file;
//                i++;
//            } else if (file.isDirectory()) {
//                getFiles(file.getAbsolutePath());
//            }
//        }

        //Log.d(TAG, String.valueOf(allFiles.size()));

        for (int i = 0; i < allFiles.size(); i++) {
            //Log.d(TAG, allFiles.get(i).getName());
        }

        return allFiles;
        //return directory.listFiles();
    }

    public static File getFileOldest(String myDirectory) {
        fList = getFiles(myDirectory); // get the file list
        File file = fList.get(0);


        if (fList.size() > 0) {
            long min = fList.get(0).lastModified();
            for (int i = 1; i < fList.size(); i++) {
                Log.d(TAG, "fList.get(i).lastModified(): " + fList.get(i).lastModified());
                if (fList.get(i).lastModified() < min && (fList.get(i).toString().endsWith("beacon.json") || !(fList.get(i).toString().endsWith(".json")))) {
                    min = fList.get(i).lastModified();
                    file = fList.get(i);
                }
            }
        }
        return file; // return the oldest file, if there is no file null will be returned
    }


    public static boolean isClosed(File file) {
        long lastModified = file.lastModified();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        //Old way of looking if file is closed
        //long fModifiedTime = (long) (((currentTime - lastModified) / (1000 * 60)) % 60);
        //new way
        long fModifiedTime = (long) (((currentTime - lastModified) / (1000 * 60)));
        Log.d(TAG, " The Filename is  "+file.getName());
        Log.d(TAG, " The value of  "+String.valueOf(fModifiedTime));
        Log.d(TAG, " The currentTime is  "+currentTime);
        Log.d(TAG, " The lastModified is  "+lastModified);

        if (fModifiedTime >= 1) {
            return true;
        } else {
            return false;
        }

    }

    public static int getFileLength(String myDirectory) {
        return getFiles(myDirectory).size();
    }


    public static void deleteFile(Uri fileUri, Context context) {
        File file = new File(fileUri.getPath());
        Log.d(TAG, String.valueOf(file.getAbsoluteFile()));
        file.delete();

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "file Deleted :" + fileUri.getPath());
            }
        }

        // context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file.getAbsoluteFile())));
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file.getAbsolutePath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);

    }

    public static String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

}
