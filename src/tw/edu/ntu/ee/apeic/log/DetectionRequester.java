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
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Class for connecting to Location Services and activity recognition updates.
 * <b>
 * Note: Clients must ensure that Google Play services is available before requesting updates.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * <p/>
 * <p/>
 * To use a DetectionRequester, instantiate it and call requestUpdates(). Everything else is done
 * automatically.
 */
public class DetectionRequester implements OnConnectionFailedListener {
    private Context mContext;

    private SharedPreferences mPrefs;

    private PendingIntent mActivityRecognitionPendingIntent;
    private PendingIntent mLocationPendingIntent;

    private ActivityRecognitionClient mActivityRecognitionClient;
    private LocationClient mLocationClient;

    public DetectionRequester(Context context) {
        mContext = context;

        mPrefs = context.getApplicationContext().getSharedPreferences(
                ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        mActivityRecognitionPendingIntent = null;
        mActivityRecognitionClient = null;
        mLocationClient = null;
    }

    public PendingIntent getRequestPendingIntent() {
        return mActivityRecognitionPendingIntent;
    }

    public void setRequestPendingIntent(PendingIntent intent) {
        mActivityRecognitionPendingIntent = intent;
    }

    public void requestUpdates() {
        getActivityRecognitionClient().connect();
        getLocationClient().connect();
    }

    /**
     * Get the current activity recognition client, or create a new one if necessary.
     * This method facilitates multiple requests for a client, even if a previous
     * request wasn't finished. Since only one client object exists while a connection
     * is underway, no memory leaks occur.
     *
     * @return An ActivityRecognitionClient object
     */
    private ActivityRecognitionClient getActivityRecognitionClient() {
        if (mActivityRecognitionClient == null) {
            mActivityRecognitionClient = new ActivityRecognitionClient(mContext, new ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    getActivityRecognitionClient().requestActivityUpdates(
                            ActivityUtils.DETECTION_INTERVAL_MILLISECONDS,
                            createRequestPendingIntent());
                    getActivityRecognitionClient().disconnect();
                }

                @Override
                public void onDisconnected() {
                    mActivityRecognitionClient = null;
                }
            }, this);
        }
        return mActivityRecognitionClient;
    }

    public LocationClient getLocationClient() {
        if (mLocationClient == null)
            mLocationClient = new LocationClient(mContext, new ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.d(ActivityUtils.APPTAG, "connected");
                    LocationRequest request = LocationRequest.create();
                    request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    request.setInterval(60000);

                    Intent intent = new Intent(mContext, LocationUpdateReceiver.class);
                    mLocationPendingIntent = PendingIntent.getBroadcast(
                            mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    getLocationClient().requestLocationUpdates(request, mLocationPendingIntent);
                    getLocationClient().disconnect();
                }

                @Override
                public void onDisconnected() {
                    mLocationClient = null;
                }
            }, this);

        return mLocationClient;
    }

    private PendingIntent createRequestPendingIntent() {
        if (getRequestPendingIntent() != null) {
            return mActivityRecognitionPendingIntent;
        } else {
            Intent intent = new Intent(mContext, AppUsageLogUpdateIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            setRequestPendingIntent(pendingIntent);
            return pendingIntent;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((Activity) mContext,
                        ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (SendIntentException e) {
                Log.e(ActivityUtils.APPTAG, e.getMessage());
            }
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    connectionResult.getErrorCode(),
                    (Activity) mContext,
                    ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }
    }
}