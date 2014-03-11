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
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;
import tw.edu.ntu.ee.arbor.apeic.R;

public class AddLogService extends IntentService {
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private ApeicPrefsUtil mPrefsUtil;

    public AddLogService() {
        // Set the label for the service's background thread
        super("AddLogService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPrefsUtil = ApeicPrefsUtil.getInstance(this);

        // datetime
        String dateTime = getDateTime();

        // location: lat, lng, acc
        double latitude = Double.longBitsToDouble(mPrefsUtil.getLongPref(ApeicPrefsUtil.KEY_LAST_LATITUDE));
        double longitude = Double.longBitsToDouble(mPrefsUtil.getLongPref(ApeicPrefsUtil.KEY_LAST_LONGITUDE));
        float locationAcc = mPrefsUtil.getFloatPref(ApeicPrefsUtil.KEY_LAST_LOCATION_ACC);
        float speed = mPrefsUtil.getFloatPref(ApeicPrefsUtil.KEY_LAST_SPEED);

        // activity: type, acc
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        int activityType = mostProbableActivity.getType();
        String activityName = getActivityName(activityType);
        int confidence = mostProbableActivity.getConfidence();
        Log.d(ApeicUtil.TAG, "ActivityRecognitionResult received: " +
                "Activity[" + activityName + " confidence=" + String.valueOf(confidence) + "]");

        // illumination
        float illumination = mPrefsUtil.getFloatPref(ApeicPrefsUtil.KEY_ILLUMINATION);

        // network status
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        String mobileConnection = (connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting()) ? "1" : "0";
        String wifiConnection = (connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting()) ? "1" : "0";
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int wifiAPNum = wifiManager.getScanResults().size();

        // battery
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPower = level / (float) scale;

        // application
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();

        // screen state
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

        String log = getString(R.string.log, dateTime, latitude, longitude, locationAcc,
                speed, activityName, confidence, illumination,
                mobileConnection, wifiConnection, wifiAPNum,
                batteryPower, (pm.isScreenOn() ? packageName : "null"));
        Log.d(ApeicUtil.TAG, log);
        LogFile.getInstance(getApplicationContext()).write(log);

        if (isMoving(activityType) && isActivityChanged(activityType) && (confidence >= 50)) {
            sendNotification();
        }
        mPrefsUtil.setIntPref(ApeicPrefsUtil.KEY_LAST_ACTIVITY_TYPE, activityType);
    }

    private String getDateTime() {
        try {
            SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateTimeInstance();
            format.applyPattern(DATE_FORMAT_PATTERN);
            format.applyLocalizedPattern(format.toLocalizedPattern());
            return format.format(new Date());
        } catch (Exception e) {
            Log.e(ApeicUtil.TAG, getString(R.string.date_format_error));
            return "";
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
        int previousType = mPrefsUtil.getIntPref(ApeicPrefsUtil.KEY_LAST_ACTIVITY_TYPE);
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