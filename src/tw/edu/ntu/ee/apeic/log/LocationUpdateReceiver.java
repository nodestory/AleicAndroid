package tw.edu.ntu.ee.apeic.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/2/18.
 */
public class LocationUpdateReceiver extends BroadcastReceiver {
    private ApeicPrefsUtil mPrefsUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        mPrefsUtil = ApeicPrefsUtil.getInstance(context);

        if (intent.hasExtra(LocationClient.KEY_LOCATION_CHANGED)) {
            Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
            updateLocation(location);
        }
    }

    private void updateLocation(Location loc) {
        Log.v(ApeicUtil.TAG, "Location update received: " + loc.toString());
        mPrefsUtil.setLongPref(ApeicPrefsUtil.KEY_LAST_LATITUDE, Double.doubleToRawLongBits(loc.getLatitude()));
        mPrefsUtil.setLongPref(ApeicPrefsUtil.KEY_LAST_LONGITUDE, Double.doubleToRawLongBits(loc.getLongitude()));
        mPrefsUtil.setFloatPref(ApeicPrefsUtil.KEY_LAST_SPEED, loc.getSpeed());
        mPrefsUtil.setFloatPref(ApeicPrefsUtil.KEY_LAST_LOCATION_ACC, loc.getAccuracy());
    }
}