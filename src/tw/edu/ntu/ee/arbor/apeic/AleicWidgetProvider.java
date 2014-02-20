package tw.edu.ntu.ee.arbor.apeic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.activityrecognition.ActivityUtils;

/**
 * Created by Linzy on 2014/2/10.
 */
public class AleicWidgetProvider extends AppWidgetProvider {
    private PendingIntent mUpdatePendingIntent;

    @Override
    public void onEnabled(Context context) {
        Log.d(ActivityUtils.APPTAG, "Widget onEnabled.");

//        startUpdateWidget(context);
        Intent intent = new Intent(context, UpdateWidgetService.class);
        ComponentName widgetName = new ComponentName(context, AleicWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widgetName);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.stopService(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(ActivityUtils.APPTAG, "Widget onUpdate.");
        startUpdateWidget(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(ActivityUtils.APPTAG, "Widget onDeleted.");
        stopUpdateWidget(context);
    }

    private void startUpdateWidget(Context context) {
        PendingIntent pendingIntent = createUpdatePendingIntent(context);
        setUpdatePendingIntent(pendingIntent);
        // TODO
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, 0, 10000, getUpdatePendingIntent());
    }

    private void stopUpdateWidget(Context context) {
        context.stopService(new Intent(context, UpdateWidgetService.class));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(createUpdatePendingIntent(context));
    }

    private PendingIntent createUpdatePendingIntent(Context context) {
        ComponentName widgetName = new ComponentName(context, AleicWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

//        int[] allWidgetIds = getAleicWidgetIds(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widgetName);
        for(int id: allWidgetIds) {
            Log.d(ActivityUtils.APPTAG, String.valueOf(id));
        }

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

    private int[] getAleicWidgetIds(Context context) {
        ComponentName widgetName = new ComponentName(context, AleicWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager.getAppWidgetIds(widgetName);
    }
}