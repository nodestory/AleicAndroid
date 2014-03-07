package tw.edu.ntu.ee.apeic.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;
import tw.edu.ntu.ee.arbor.apeic.R;

/**
 * Created by Linzy on 2014/2/20.
 */
public class UpdateWidgetService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(ApeicUtil.TAG, "UpdateWidgetService onStartCommand.");

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        updateWidget(allWidgetIds);
        // TODO
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(ApeicUtil.TAG, "UpdateWidgetService onDestroy.");
    }

    // TODO
    private void updateWidget(int[] widgetIds) {
        String[] apps = getMostProbableApps();
        String temp = "";
        for (int i = 0; i < 4; i++) {
            temp += (apps[i] + " ");
        }
        Log.d(ApeicUtil.TAG, temp);
        ApeicPrefsUtil.getInstance(this).setStringPref("apps", temp);

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
//        views.setTextViewText(R.id.textView_location,
//                getString(R.string.widget_current_location,
//                        ApeicPrefsUtil.getStringPref(this, ApeicPrefsUtil.KEY_LAST_LATITUDE),
//                        ApeicPrefsUtil.getStringPref(this, ApeicPrefsUtil.KEY_LAST_LONGITUDE)));
//        views.setTextViewText(R.id.textView_activity,
//                getString(R.string.widget_current_activity,
//                        ApeicPrefsUtil.getStringPref(this,
//                                ApeicPrefsUtil.KEY_LAST_ACTIVITY_TYPE)));

        for (int widgetId : widgetIds) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

            Intent updateWidgetIntent = new Intent(this, UpdateWidgetRemoteViewsService.class);
            updateWidgetIntent.putExtra("apps", apps);
            views.setRemoteAdapter(R.id.gridView, updateWidgetIntent);
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.gridView);


            Intent handleItemClickedIntent = new Intent();
            handleItemClickedIntent.setAction(ApeicUtil.ACTION_ITEM_CLICKED);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                    handleItemClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.gridView, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, views);
        }


        Log.d(ApeicUtil.TAG, "Widget updated.");
    }

    private String[] getMostProbableApps() {
        PackageManager pm = getPackageManager();

        List<ApplicationInfo> installedApps =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> enabledApps = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : installedApps) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                enabledApps.add(info);
            }
        }
        Collections.shuffle(enabledApps);

        String[] mostProbableApps = new String[4];
        for (int i = 0; i < 4; i++) {
            mostProbableApps[i] = enabledApps.get(i).packageName;
        }
        return mostProbableApps;
    }
}