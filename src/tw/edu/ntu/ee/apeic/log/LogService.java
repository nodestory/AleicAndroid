package tw.edu.ntu.ee.apeic.log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by Linzy on 2014/3/8.
 */
public class LogService extends Service {
    private DetectionRequester mRequester = new DetectionRequester(this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isServicesConnected()) mRequester.requestUpdates();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public  void  onDestroy() {
        super.onDestroy();
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
}