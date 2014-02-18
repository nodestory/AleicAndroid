package com.example.android.activityrecognition;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by Linzy on 2014/2/18.
 */
public abstract class Detector implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private Context mContext;
    private PendingIntent mPendingIntent;
    private Object mClinet;

    Detector(Context context) {
        mContext = context;
    }

    public PendingIntent getRequestPendingIntent() {
        return mPendingIntent;
    }

    public void setRequestPendingIntent(PendingIntent intent) {
        mPendingIntent = intent;
    }

//    abstract Object getClient();
    abstract void onStartRequestUpdate();

    @Override
    public void onConnected(Bundle bundle) {
        onStartRequestUpdate();
    }

    @Override
    public void onDisconnected() {
        mClinet = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((Activity) mContext,
                        ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
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