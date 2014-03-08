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
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import tw.edu.ntu.ee.apeic.ApeicUtil;

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

    private ApeicUtil.REQUEST_TYPE mRequestType;

    // Activity
    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mActivityRecognitionPendingIntent;

    // Location
    private LocationClient mLocationClient;
    private PendingIntent mLocationPendingIntent;

    public DetectionRequester(Context context) {
        mContext = context;

        mActivityRecognitionClient = null;
        mActivityRecognitionPendingIntent = null;
        mLocationClient = null;
        mLocationPendingIntent = null;
    }

    public void requestUpdates() {
        mRequestType = ApeicUtil.REQUEST_TYPE.ADD;
        getActivityRecognitionClient().connect();
        getLocationClient().connect();
    }

    public void removeUpdates() {
        mRequestType = ApeicUtil.REQUEST_TYPE.REMOVE;
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
                    Log.d(ApeicUtil.TAG, "ActivityRecognitionClient connected: " + mRequestType.name());
                    if (mRequestType == ApeicUtil.REQUEST_TYPE.ADD) {
                        getActivityRecognitionClient().requestActivityUpdates(
                                ApeicUtil.DETECTION_INTERVAL_MILLISECONDS,
                                createActivityRequestPendingIntent());
                        getActivityRecognitionClient().disconnect();
                    } else if (mRequestType == ApeicUtil.REQUEST_TYPE.REMOVE) {
                        getActivityRecognitionClient().removeActivityUpdates(mActivityRecognitionPendingIntent);
                        mActivityRecognitionPendingIntent.cancel();
                        getActivityRecognitionClient().disconnect();
                    }
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
                    Log.d(ApeicUtil.TAG, "LocationClient connected: " + mRequestType.name());
                    if (mRequestType == ApeicUtil.REQUEST_TYPE.ADD) {
                        LocationRequest request = LocationRequest.create();
                        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        request.setInterval(60000);

                        getLocationClient().requestLocationUpdates(request, createLocationRequestPendingIntent());
                        getLocationClient().disconnect();
                    } else if (mRequestType == ApeicUtil.REQUEST_TYPE.REMOVE) {
                        getLocationClient().removeLocationUpdates(mLocationPendingIntent);
                        mLocationPendingIntent.cancel();
                        getLocationClient().disconnect();
                    }
                }

                @Override
                public void onDisconnected() {
                    mLocationClient = null;
                }
            }, this);

        return mLocationClient;
    }

    private PendingIntent createActivityRequestPendingIntent() {
        if (mActivityRecognitionPendingIntent == null) {
            Intent intent = new Intent(mContext, AddLogService.class);
            mActivityRecognitionPendingIntent = PendingIntent.getService(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mActivityRecognitionPendingIntent;
    }

    private PendingIntent createLocationRequestPendingIntent() {
        if (mLocationPendingIntent == null) {
            Intent intent = new Intent(mContext, LocationUpdateReceiver.class);
            mLocationPendingIntent = PendingIntent.getBroadcast(
                    mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return mLocationPendingIntent;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((Activity) mContext,
                        ApeicUtil.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (SendIntentException e) {
                Log.e(ApeicUtil.TAG, e.getMessage());
            }
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    connectionResult.getErrorCode(),
                    (Activity) mContext,
                    ApeicUtil.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }
    }
}