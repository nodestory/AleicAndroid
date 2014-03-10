package tw.edu.ntu.ee.apeic.log;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/7.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ApeicUtil.TAG, "" + "BootCompletedReceiver onReceive.");

        Intent requestUpdatesIntent = new Intent(context, LogService.class);
        context.startService(requestUpdatesIntent);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent checkUploadIntent = new Intent(context, UploadCheckReceiver.class);
        am.setRepeating(AlarmManager.RTC, 0, ApeicUtil.LOG_FILE_UPLOAD_INTERVAL_MILLISECONDS,
                PendingIntent.getBroadcast(context, 0,
                        checkUploadIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}