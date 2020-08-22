package com.orpheusdroid.screenrecorder;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class JsonUtil {
    public static String toJson(Video video) {
        try {
            // Here we convert Java Object to JSON
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userID", video.getUserID());
            jsonObj.put("videoID", video.getVideoPath());
            jsonObj.put("startTime", video.getStartTime());
            jsonObj.put("endTime", video.getEndTime());
            jsonObj.put("timeZone", video.timeZone.getRawOffset());
            jsonObj.put("isLocationGranted", video.isLocationGranted());

            // create jsonArray for location
            JSONArray jsonLocationArr = new JSONArray();
            for(MyLocation loc : video.getLocationList()){
                JSONObject locObj = new JSONObject();
                locObj.put("locationLatitude", loc.getLatitude());
                locObj.put("locationAltitude", loc.getAltitude());
                locObj.put("locationLong", loc.getLong());
                locObj.put("locationTimeStamp", loc.getTimeStamp());
                locObj.put("locationAccuracy", loc.getLocationAcc());
                jsonLocationArr.put(locObj);
            }
            jsonObj.put("location",jsonLocationArr);

            // create jsonArray for apps
            JSONArray jsonAppArr = new JSONArray();
            for(App app : video.getAppList()){
                JSONObject appObj = new JSONObject();
                appObj.put("appName",app.getAppName());
                appObj.put("locationTimeStamp", app.getTimeStamp());
                jsonAppArr.put(appObj);
            }
            jsonObj.put("app",jsonAppArr);

            return jsonObj.toString();

        } catch (JSONException e) {
            Log.d(Const.TAG, e.toString());
        }
        return null;
    }
    public static String toJson(ProximityData proximityData) {
        try {
            ArrayList<ProximityDataPoint> proximities = proximityData.getOldProximities();
            ArrayList<AreaThresholds> areaThresholds = proximityData.getAreaThresholds();
            // Here we convert Java Object to JSON
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userID", proximityData.getUserID());
            jsonObj.put("uuid", proximityData.getBeaconUuid());
            jsonObj.put("startTime", proximityData.getTimeStart());
            jsonObj.put("endTime", proximityData.getTimeEnd());
            jsonObj.put("timeZone", proximityData.timeZone.getRawOffset());
            
            
            // create jsonArray for apps
            JSONArray jsonAppArr = new JSONArray();
            if(areaThresholds!=null) {
                for (AreaThresholds areaThreshold : areaThresholds) {
                    JSONObject appObj = new JSONObject();
                    appObj.put("areaName", areaThreshold.getAreaName());
                    appObj.put("thresholdProximity", areaThreshold.getThresholdProximity());
                    jsonAppArr.put(appObj);
                }
            }
            jsonObj.put("areaThresholds",jsonAppArr);


            // create jsonArray for location
            JSONArray jsonProximityArr = new JSONArray();
            for(ProximityDataPoint dataPoint : proximities){
                JSONObject locObj = new JSONObject();
                locObj.put("timestamp", dataPoint.getTimestamp());
                locObj.put("proximityPoint", dataPoint.getProximityPoint());
                jsonProximityArr.put(locObj);
            }
            jsonObj.put("proximityPoints",jsonProximityArr);

            return jsonObj.toString();

        } catch (JSONException e) {
            Log.d(Const.TAG, e.toString());
        }
        return null;
    }


    public static void writeJsonFile(String jsonString, String fileName){
        try (FileWriter file = new FileWriter(MyDirectory.path+ File.separator+ fileName +".json")) {
            Log.d(Const.TAG, "Json file" + MyDirectory.path + File.separator+ fileName +".json" );
            file.write(jsonString);
            file.flush();
            Log.d("JSON",jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(jsonString);
    }


}
