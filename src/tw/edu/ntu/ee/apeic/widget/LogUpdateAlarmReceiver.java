package tw.edu.ntu.ee.apeic.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tw.edu.ntu.ee.apeic.ApeicUtil;

import java.util.Calendar;

/**
 * Created by Linzy on 2014/2/18.
 */
public class LogUpdateAlarmReceiver extends BroadcastReceiver {
    private AlarmManager mAlarmMgr;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(ApeicUtil.APPTAG, "Log update received!!!!!");
    }

    public void setAlarm(Context context) {
        mAlarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LogUpdateAlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        mAlarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000,  mPendingIntent);
    }
}