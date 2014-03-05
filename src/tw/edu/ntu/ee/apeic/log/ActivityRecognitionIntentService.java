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

package tw.edu.ntu.ee.apeic.log;

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

import tw.edu.ntu.ee.arbor.apeic.R;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {

    // Formats the timestamp in the log
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ssZ";

    // Delimits the timestamp from the log info
    private static final String LOG_DELIMITER = ";;";

    // A date formatter
    private SimpleDateFormat mDateFormat;

    // Store the app's shared preferences repository
    private SharedPreferences mPrefs;

    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Get a handle to the repository
        mPrefs = getApplicationContext().getSharedPreferences(
                ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get a date formatter, and catch errors in the returned timestamp
        try {
            mDateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        } catch (Exception e) {
            Log.e(ActivityUtils.APPTAG, getString(R.string.date_format_error));
        }

        // Format the timestamp according to the pattern, then localize the pattern
        mDateFormat.applyPattern(DATE_FORMAT_PATTERN);
        mDateFormat.applyLocalizedPattern(mDateFormat.toLocalizedPattern());

        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Log the update
//            logActivityRecognitionResult(result);

            // Make a timestamp
            String timeStamp = mDateFormat.format(new Date());

            double latitude = Double.longBitsToDouble(
                    mPrefs.getLong(ActivityUtils.KEY_PREVIOUS_LATITUDE, -1));
            double longitude = Double.longBitsToDouble(
                    mPrefs.getLong(ActivityUtils.KEY_PREVIOUS_LONGITUDE, -1));
            float accuracy = mPrefs.getFloat(ActivityUtils.KEY_PREVIOUS_LOCATION_ACC, -1);

            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            // Get the type of activity
            int activityType = mostProbableActivity.getType();
            // Get the name of activity
            String activityName = getNameFromType(activityType);
            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();
            Editor editor = mPrefs.edit();
            editor.putInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activityType);
            editor.commit();

            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();

            LogFile.getInstance(getApplicationContext()).write(
                    timeStamp + LOG_DELIMITER +
                            getString(R.string.log_location, latitude, longitude, accuracy) + LOG_DELIMITER +
                            getString(R.string.log_activity, activityName, confidence) + LOG_DELIMITER +
                            (pm.isScreenOn() ? packageName : "null")
            );

            /* If the current type is "moving"
             * The activity has changed from the previous activity
             * The confidence level for the current activity is > 50%
             */
            if (isMoving(activityType) && activityChanged(activityType) && (confidence >= 50)) {
                // Notify the user
                sendNotification();
            }
        }
    }

    /**
     * Post a notification to the user. The notification prompts the user to click it to
     * open the device's GPS settings
     */
    private void sendNotification() {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.turn_on_GPS))
                .setSmallIcon(R.drawable.ic_notification)

                        // Get the Intent that starts the Location settings panel
                .setContentIntent(getContentIntent());

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }

    /**
     * Get a content Intent for the notification
     *
     * @return A PendingIntent that starts the device's Location Settings panel.
     */
    private PendingIntent getContentIntent() {

        // Set the Intent action to open Location Settings
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Tests to see if the activity has changed
     *
     * @param currentType The current activity type
     * @return true if the user's current activity is different from the previous most probable
     * activity; otherwise, false.
     */
    private boolean activityChanged(int currentType) {

        // Get the previous type, otherwise return the "unknown" type
        int previousType = mPrefs.getInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE,
                DetectedActivity.UNKNOWN);

        // If the previous type isn't the same as the current type, the activity has changed
        if (previousType != currentType) {
            return true;

            // Otherwise, it hasn't.
        } else {
            return false;
        }
    }

    /**
     * Determine if an activity means that the user is moving.
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    private boolean isMoving(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
                return false;
            default:
                return true;
        }
    }

    /**
     * Write the activity recognition update to the log file
     *
     * @param result The result extracted from the incoming Intent
     */
    private void logActivityRecognitionResult(ActivityRecognitionResult result) {
        // Get all the probably activities from the updated result
        Log.i("LOG", String.valueOf(result.getProbableActivities().size()));
        for (DetectedActivity detectedActivity : result.getProbableActivities()) {
            double latitude = Double.longBitsToDouble(
                    mPrefs.getLong(ActivityUtils.KEY_PREVIOUS_LATITUDE, -1));
            double longitude = Double.longBitsToDouble(
                    mPrefs.getLong(ActivityUtils.KEY_PREVIOUS_LONGITUDE, -1));
            float accuracy = mPrefs.getFloat(ActivityUtils.KEY_PREVIOUS_LOCATION_ACC, -1);

            // Get the activity type, confidence level, and human-readable name
            int activityType = detectedActivity.getType();
            int confidence = detectedActivity.getConfidence();
            String activityName = getNameFromType(activityType);

            // Make a timestamp
            String timeStamp = mDateFormat.format(new Date());

            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            pm.isScreenOn();

            ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();

            // Get the current log file or create a new one, then log the activity
            LogFile.getInstance(getApplicationContext()).write(
                    timeStamp + LOG_DELIMITER +
                            getString(R.string.log_location, latitude, longitude, accuracy) + LOG_DELIMITER +
                            getString(R.string.log_activity, activityName, confidence) + LOG_DELIMITER +
                            (pm.isScreenOn() ? packageName : "null")
            );
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}