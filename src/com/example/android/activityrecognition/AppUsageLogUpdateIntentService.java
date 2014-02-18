/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.activityrecognition;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUsageLogUpdateIntentService extends IntentService {
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ssZ";
    private static final String LOG_DELIMITER = ";;";

    private SharedPreferences mPrefs;

    private SimpleDateFormat mDateFormat;

    public AppUsageLogUpdateIntentService() {
        // Set the label for the service's background thread
        super("AppUsageLogUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPrefs = getApplicationContext().getSharedPreferences(
                ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            String timeStamp = "";
            try {
                mDateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                mDateFormat.applyPattern(DATE_FORMAT_PATTERN);
                mDateFormat.applyLocalizedPattern(mDateFormat.toLocalizedPattern());
                timeStamp = mDateFormat.format(new Date());
            } catch (Exception e) {
                Log.e(ActivityUtils.APPTAG, getString(R.string.date_format_error));
            }

            double latitude = Double.longBitsToDouble(
                    mPrefs.getLong(ActivityUtils.KEY_PREVIOUS_LATITUDE, -1));
            double longitude = Double.longBitsToDouble(
                    mPrefs.getLong(ActivityUtils.KEY_PREVIOUS_LONGITUDE, -1));
            float accuracy = mPrefs.getFloat(ActivityUtils.KEY_PREVIOUS_LOCATION_ACC, -1);

            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int activityType = mostProbableActivity.getType();
            String activityName = getActivityName(activityType);
            int confidence = mostProbableActivity.getConfidence();
            Editor editor = mPrefs.edit();
            editor.putInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activityType);
            editor.commit();

            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();

            LogFile.getInstance(getApplicationContext()).log(
                    timeStamp + LOG_DELIMITER +
                            getString(R.string.log_location, latitude, longitude, accuracy) + LOG_DELIMITER +
                            getString(R.string.log_activity, activityName, confidence) + LOG_DELIMITER +
                            (pm.isScreenOn() ? packageName : "null")
            );

            if (isMoving(activityType) && isActivityChanged(activityType) && (confidence >= 50)) {
                sendNotification();
            }
        }
    }

    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";

        }
        return "UNKNOWN";
    }

    private boolean isActivityChanged(int currentType) {
        int previousType = mPrefs.getInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE,
                DetectedActivity.UNKNOWN);
        return (currentType != previousType) ? true : false;
    }

    private boolean isMoving(int type) {
        switch (type) {
            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
                return false;
            default:
                return true;
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.turn_on_GPS))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(getContentIntent());

        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(0, builder.build());
    }

    private PendingIntent getContentIntent() {
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        return PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}