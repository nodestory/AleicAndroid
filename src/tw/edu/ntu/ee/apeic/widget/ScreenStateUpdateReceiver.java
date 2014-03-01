package tw.edu.ntu.ee.apeic.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tw.edu.ntu.ee.apeic.log.ActivityUtils;

/**
 * Created by Linzy on 2014/2/20.
 */
public class ScreenStateUpdateReceiver extends BroadcastReceiver {
    private PendingIntent mUpdatePendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d(ActivityUtils.APPTAG, "Screen state changed: ON.");
            if (isWidgetAlive(context))
                startUpdateWidget(context);
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d(ActivityUtils.APPTAG, "Screen state changed: OFF.");
            if (isWidgetAlive(context))
                stopUpdateWidgetService(context);
        }
    }

    private void startUpdateWidget(Context context) {
        PendingIntent pendingIntent = createUpdatePendingIntent(context);
        setUpdatePendingIntent(pendingIntent);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // TODO
        manager.setRepeating(AlarmManager.RTC, 0, 10000, getUpdatePendingIntent());
    }

    private void stopUpdateWidgetService(Context context) {
        context.stopService(new Intent(context, UpdateWidgetService.class));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // TODO
        manager.cancel(createUpdatePendingIntent(context));
    }

    private PendingIntent createUpdatePendingIntent(Context context) {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getAllWidgetIDs(context));
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

    private boolean isWidgetAlive(Context context) {
        return true ? getAllWidgetIDs(context).length > 0 : false;
    }

    private int[] getAllWidgetIDs(Context context) {
        ComponentName widgetName = new ComponentName(context, AleicWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widgetName);
        return allWidgetIds;
    }
}