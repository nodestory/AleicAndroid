package tw.edu.ntu.ee.apeic.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

/**
 * Created by Linzy on 2014/2/18.
 */
public class LocationUpdateReceiver extends BroadcastReceiver {
    private SharedPreferences mPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        mPrefs = context.getApplicationContext().getSharedPreferences(
                ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        if (intent.hasExtra(LocationClient.KEY_LOCATION_CHANGED)) {
            Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);
            updateLocation(location);
        }
    }

    private void updateLocation(Location loc) {
        Log.d(ActivityUtils.APPTAG, "Location update received: " + loc.toString());
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(ActivityUtils.KEY_PREVIOUS_LATITUDE, Double.doubleToRawLongBits(loc.getLatitude()));
        editor.putLong(ActivityUtils.KEY_PREVIOUS_LONGITUDE, Double.doubleToRawLongBits(loc.getLongitude()));
        editor.putFloat(ActivityUtils.KEY_PREVIOUS_LOCATION_ACC, loc.getAccuracy());
        editor.commit();
    }
}