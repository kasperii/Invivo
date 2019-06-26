/*
 * Copyright (c) 2016-2017. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.orpheusdroid.screenrecorder;

/**
 * Created by vijai on 12-10-2016.
 */

// POJO class for bunch of statics used across the app
public class Const {
    public static final int VIDEO_EDIT_REQUEST_CODE = 1004;
    public static final int VIDEO_EDIT_RESULT_CODE = 1005;
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 500; // 200 meters
    public static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 1 minute
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    public static final String TAG = "SCREENRECORDER_LOG";
    public static final String APPDIR = "screenrecorder";
    public static final String ALERT_EXTR_STORAGE_CB_KEY = "ext_dir_warn_donot_show_again";
    public static final String VIDEO_EDIT_URI_KEY = "edit_video";


    public static final String TAG_My = "Uploader_LOG";
    public static final String PREFS_NAME = "Ref";
    public static final String FILE_URI = "FileURI";
    public static final String FILE_NAME = "fName";
    public static final String SESSION_KEY = "UploadingSession";
    public static final String EMPTY_DIRECTORY = "NoFiles";


    static final String FILE_UPLOADING_START= "com.orpheusdroid.screenrecorder.services.action.startuploading";
    static final String FILE_UPLOADING_PAUSE = "com.orpheusdroid.screenrecorder.services.action.pauseuploading";
    static final String FILE_UPLOADING_RESUME = "com.orpheusdroid.screenrecorder.services.action.resumeuploading";
    static final String FILE_UPLOADING_STOP = "com.orpheusdroid.screenrecorder.services.action.stopuploading";
    static final String USER_CONNECT = "com.orpheusdroid.screenrecorder.action.CONNECT";
    static final String GROUP_KEY_WORK_Recorder = "om.orpheusdroid.screenrecorder.RECORDER";


    static final int EXTDIR_REQUEST_CODE = 1000;
    static final int AUDIO_REQUEST_CODE = 1001;
    static final int SYSTEM_WINDOWS_CODE = 1002;
    static final int SCREEN_RECORD_REQUEST_CODE = 1003;
    static final int LOCATION_REQUEST_CODE = 2333;
    static final String SCREEN_RECORDING_START = "com.orpheusdroid.screenrecorder.services.action.startrecording";
    static final String SCREEN_RECORDING_PAUSE = "com.orpheusdroid.screenrecorder.services.action.pauserecording";
    static final String SCREEN_RECORDING_SKIP = "com.orpheusdroid.screenrecorder.services.action.skiprecording";
    static final String SCREEN_RECORDING_RESUME = "com.orpheusdroid.screenrecorder.services.action.resumerecording";
    static final String SCREEN_RECORDING_STOP = "com.orpheusdroid.screenrecorder.services.action.stoprecording";
    static final String SCREEN_RECORDING_RESTART = "com.orpheusdroid.screenrecorder.services.action.restartrecording";
    static final String SCREEN_RECORDING_DESTORY_SHAKE_GESTURE = "com.orpheusdroid.screenrecorder.services.action.destoryshakegesture";
    static final int SCREEN_RECORDER_NOTIFICATION_ID = 5001;
    static final int SCREEN_RECORDER_SHARE_NOTIFICATION_ID = 5002;
    static final int SCREEN_RECORDER_WAITING_FOR_SHAKE_NOTIFICATION_ID = 5003;
    static final String RECORDER_INTENT_DATA = "recorder_intent_data";
    static final String RECORDER_INTENT_RESULT = "recorder_intent_result";
    static final String RECORDING_NOTIFICATION_CHANNEL_ID = "recording_notification_channel_id1";
    static final String UPLOADING_NOTIFICATION_CHANNEL_ID = "uploading_notification_channel_id1";
    static final String SHARE_NOTIFICATION_CHANNEL_ID = "share_notification_channel_id1";
    static final String RECORDING_NOTIFICATION_CHANNEL_NAME = "Persistent notification shown when recording screen or when waiting for shake gesture";
    static final String UPLOADING_NOTIFICATION_CHANNEL_NAME = "Persistent notification shown when uploading screen or when waiting for the file to close";
    static final String SHARE_NOTIFICATION_CHANNEL_NAME = "Notification shown to share or edit the recorded video";
    static final String ANALYTICS_URL = "https://analytics.orpheusdroid.com";
    static final String ANALYTICS_API_KEY = "07273a5c91f8a932685be1e3ad0d160d3de6d4ba";

    static final String PREFS_REQUEST_ANALYTICS_PERMISSION = "request_analytics_permission";
    static final String PREFS_LIGHT_THEME = "light_theme";
    static final String PREFS_DARK_THEME = "dark_theme";
    static final String PREFS_BLACK_THEME = "black_theme";
    static final String UPLOAD = "Uploading Files";
    static final String RESUME = "Resuming Files";
    static final String NO_CHARGER = "Uploading Paused";
    static final String WAITING_FILES_CLOSE =  "Uploader is Pending";
    static final String NO_FILE_TO_BE_UPLOADED = "Empty Directory";
    static final String FILE_IS_DELETED = "File is deleted";
    static final String FILE_IS_NOT_CLOSED = "One of the recorded files is not closed yet or the recorder is still recording. Try to plug in the charger after 2 min of stopping the recorder";

    static final String LOOKING_FOR_FILES = "Looking_For_File";

    public static final String DEVICE_ID = "device_id";

    public enum RecordingState {
        RECORDING, PAUSED, STOPPED, START
    }

    public enum analytics {
        CRASHREPORTING, USAGESTATS
    }

    public enum UploadingState{
        UPLOAD, PAUSE, STP
    }
}
