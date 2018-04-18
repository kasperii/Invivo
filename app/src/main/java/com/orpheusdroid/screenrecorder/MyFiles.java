package com.orpheusdroid.screenrecorder;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Calendar;

public class MyFiles {

    public static final String TAG = "MyFiles";
    private static File[] fList;
    private static File directory = new File( MyDirectory.path);


    public static File[] getFiles() {
        Log.d(TAG, "Files path: " + MyDirectory.path);
        return directory.listFiles();
    }


    public static File getFileOldest(){
        fList = getFiles(); // get the file list
        File file = fList[0];
        if (fList.length > 0){
            long max = fList[0].lastModified();
            for (int i = 0; i<fList.length; i++ ){
                if (fList[0].lastModified()> max){
                    max = fList[0].lastModified();
                    file = fList[0];
                }
            }

        }
        return file; // return the oldest file, if there is no file null will be returned
    }


    public static boolean isClosed(File file) {
        long lastModified = file.lastModified();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long fModifiedTime = (long) (((currentTime - lastModified) / (1000 * 60)) % 60);
        Log.d(TAG, String.valueOf(fModifiedTime));


        if (fModifiedTime >= 1){
            return true;
        }
        else{
            return false;
        }

    }

    public static int getFileLength(){
        return getFiles().length;
    }


    public static void deleteFile(Uri fileUri) {
        File fdelete = new File(fileUri.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d(TAG, "file Deleted :" + fileUri.getPath());
            }
        }
    }

    public static String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

}
