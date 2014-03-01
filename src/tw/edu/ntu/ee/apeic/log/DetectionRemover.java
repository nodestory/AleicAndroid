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
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;

import tw.edu.ntu.ee.apeic.ApeicUtil;
import tw.edu.ntu.ee.arbor.apeic.R;

/**
 * Class for connecting to Location Services and removing activity recognition updates.
 * <b>
 * Note: Clients must ensure that Google Play services is available before removing activity 
 * recognition updates.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 *
 *
 * To use a DetectionRemover, instantiate it, then call removeUpdates().
 *
 */
public class DetectionRemover implements ConnectionCallbacks, OnConnectionFailedListener {
    private Context mContext;

    private ActivityRecognitionClient mActivityRecognitionClient;
    private LocationClient mLocationClient;

    // The PendingIntent sent in removeUpdates()
    private PendingIntent mCurrentIntent;


    /**
     * Construct a DetectionRemover for the current Context
     *
     * @param context A valid Context
     */
    public DetectionRemover(Context context) {
        mContext = context;

        mActivityRecognitionClient = null;
        mLocationClient = null;
    }

    /**
     * Remove the activity recognition updates associated with a PendIntent. The PendingIntent is 
     * the one used in the request to add activity recognition updates.
     *
     * @param requestIntent The PendingIntent used to request activity recognition updates
     */
    public void removeUpdates(PendingIntent requestIntent) {

        /*
         * Set the request type, store the List, and request a activity recognition client
         * connection.
         */
        mCurrentIntent = requestIntent;

        // Continue the removal by requesting a connection
        requestConnection();
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getActivityRecognitionClient().connect();
    }

    /**
     * Get the current activity recognition client, or create a new one if necessary.
     *
     * @return An ActivityRecognitionClient object
     */
    public ActivityRecognitionClient getActivityRecognitionClient() {
        /*
         * If a client doesn't already exist, create a new one, otherwise
         * return the existing one. This allows multiple attempts to send
         * a request without causing memory leaks by constantly creating
         * new clients.
         *
         */
        if (mActivityRecognitionClient == null) {
            // Create a new one
            setActivityRecognitionClient(new ActivityRecognitionClient(mContext, this, this));
        }
        return mActivityRecognitionClient;
    }

    /**
     * Get a activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // Disconnect the client
        getActivityRecognitionClient().disconnect();

        // Set the client to null
        setActivityRecognitionClient(null);
    }

    /**
     * Set the global activity recognition client
     * @param client An ActivityRecognitionClient object
     */
    public void setActivityRecognitionClient(ActivityRecognitionClient client) {
        mActivityRecognitionClient = client;

    }

    /*
     * Called by Location Services once the activity recognition client is connected.
     *
     * Continue by removing activity recognition updates.
     */
    @Override
    public void onConnected(Bundle connectionData) {
        // If debugging, log the connection
        Log.d(ApeicUtil.APPTAG, mContext.getString(R.string.connected));
        // Send a request to Location Services to remove activity recognition updates
        continueRemoveUpdates();
    }

    /**
     * Once the connection is available, send a request to remove activity recognition updates. 
     */
    private void continueRemoveUpdates() {
        
        // Remove the updates
        // TODO
        Log.d(ApeicUtil.APPTAG, String.valueOf(getActivityRecognitionClient() == null));
        Log.d(ApeicUtil.APPTAG, String.valueOf(mCurrentIntent == null));
        mActivityRecognitionClient.removeActivityUpdates(mCurrentIntent);
        
        /*
         * Cancel the PendingIntent. This stops Intents from arriving at the IntentService, even if
         * request fails. 
         */
        mCurrentIntent.cancel();
        
        // Disconnect the client
        requestDisconnection();
    }

    /*
     * Called by Location Services once the activity recognition client is disconnected.
     */
    @Override
    public void onDisconnected() {

        // In debug mode, log the disconnection
        Log.d(ApeicUtil.APPTAG, mContext.getString(R.string.disconnected));

        // Destroy the current activity recognition client
        mActivityRecognitionClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((Activity) mContext,
                        ApeicUtil.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (SendIntentException e) {
                Log.e(ApeicUtil.APPTAG, e.getMessage());
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
