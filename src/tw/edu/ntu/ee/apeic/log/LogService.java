package tw.edu.ntu.ee.apeic.log;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/8.
 */
public class LogService extends Service implements SensorEventListener {
    public static final int SCREEN_OFF_RECEIVER_DELAY = 500;

    private SensorManager mSensorManager = null;
    public BroadcastReceiver mSensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                return;
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    unregisterListener();
                    registerListener();
                }
            };

            new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
        }
    };

    private DetectionRequester mRequester = new DetectionRequester(this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerReceiver(mSensorReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isServicesConnected()) mRequester.requestUpdates();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSensorReceiver);
        unregisterListener();

        if (isServicesConnected()) mRequester.removeUpdates();
    }

    private boolean isServicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            return false;
        }
    }

    private void registerListener() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterListener() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            ApeicPrefsUtil prefsUtil = ApeicPrefsUtil.getInstance(this);
            if (!prefsUtil.getPrefs().contains(ApeicPrefsUtil.KEY_MAX_ILLUMINATION)) {
                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                prefsUtil.setFloatPref(ApeicPrefsUtil.KEY_MAX_ILLUMINATION, lightSensor.getMaximumRange());
            }
            float illumination =
                    sensorEvent.values[0]/prefsUtil.getFloatPref(ApeicPrefsUtil.KEY_MAX_ILLUMINATION);
            prefsUtil.setFloatPref(ApeicPrefsUtil.KEY_ILLUMINATION, illumination);
            Log.d(ApeicUtil.TAG, "onSensorChanged: " + illumination);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}