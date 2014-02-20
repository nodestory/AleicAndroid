package tw.edu.ntu.ee.arbor.apeic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.activityrecognition.ActivityUtils;

/**
 * Created by Linzy on 2014/2/20.
 */
public class ScreenStateUpdateReceiver extends BroadcastReceiver {
    private PendingIntent mUpdatePendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d(ActivityUtils.APPTAG, "Screen state changed: ON.");
            startUpdateWidget(context);
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d(ActivityUtils.APPTAG, "Screen state changed: OFF.");
            stopUpdateWidgetService(context);
        }
    }

    private void startUpdateWidget(Context context) {
        PendingIntent pendingIntent = createUpdatePendingIntent(context);
        setUpdatePendingIntent(pendingIntent);
        // TODO
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, 0, 10000, getUpdatePendingIntent());
    }

    private void stopUpdateWidgetService(Context context) {
        context.stopService(new Intent(context, UpdateWidgetService.class));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // TODO
        manager.cancel(createUpdatePendingIntent(context));
    }

    private PendingIntent createUpdatePendingIntent(Context context) {
        // TODO
        ComponentName widgetName = new ComponentName(context, AleicWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widgetName);

        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getService(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent getUpdatePendingIntent() {
        return mUpdatePendingIntent;
    }

    private void setUpdatePendingIntent(PendingIntent pendingIntent) {
        mUpdatePendingIntent = pendingIntent;
    }
}