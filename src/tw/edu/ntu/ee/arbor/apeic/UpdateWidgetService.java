package tw.edu.ntu.ee.arbor.apeic;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.activityrecognition.ActivityUtils;
import com.example.android.activityrecognition.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Linzy on 2014/2/20.
 */
public class UpdateWidgetService extends Service {
    private static final int[] SHORTCUT_IDS = {R.id.imageButton_app1, R.id.imageButton_app2,
            R.id.imageButton_app3, R.id.imageButton_app4};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ActivityUtils.APPTAG, "UpdateWidgetService onStartCommand.");

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        updateWidget(allWidgetIds);
        // TODO
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ActivityUtils.APPTAG, "UpdateWidgetService onDestroy.");
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        return pm.isScreenOn();
    }

    private void updateWidget(int[] widgetIds) {
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

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
        for (int i = 0; i < 4; i++) {
            ApplicationInfo info = enabledApps.get(i);
            Drawable icon = pm.getApplicationIcon(info);
            views.setImageViewBitmap(SHORTCUT_IDS[i], ((BitmapDrawable) icon).getBitmap());

            Intent startActivityIntent = pm.getLaunchIntentForPackage(info.packageName);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0);
            views.setOnClickPendingIntent(SHORTCUT_IDS[i], pendingIntent);
        }

        for (int widgetId : widgetIds) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            appWidgetManager.updateAppWidget(widgetId, views);
        }

        Log.d(ActivityUtils.APPTAG, "Widget updated.");
    }
}