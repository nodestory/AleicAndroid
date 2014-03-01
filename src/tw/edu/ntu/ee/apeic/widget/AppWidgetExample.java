package tw.edu.ntu.ee.apeic.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Linzy on 2014/2/10.
 */
public class AppWidgetExample extends AppWidgetProvider {
    /**
     * Called when the activity is first created.
     */
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int IDs = appWidgetIds.length;
        for (int i = 0; i < IDs; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void onDeleted(Context context, int[] appWidgetIds) {

    }

    private void updateAppWidget(Context context,
                                 AppWidgetManager appWidgetManager, int appWidgetId) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> installedApps =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> enabledApps = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : installedApps) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                enabledApps.add(info);
            }
        }

        Collections.shuffle(enabledApps);

//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
//        int[] shortcutIds = {R.id.imageButton_app1, R.id.imageButton_app2,
//                R.id.imageButton_app3, R.id.imageButton_app4};
//        for (int i = 0; i < 4; i++) {
//            ApplicationInfo info = enabledApps.get(i);
//            Drawable icon = pm.getApplicationIcon(info);
//            views.setImageViewBitmap(shortcutIds[i], ((BitmapDrawable) icon).getBitmap());
//            Intent intent = pm.getLaunchIntentForPackage(info.packageName);
////            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            Log.d("test", info.packageName);
//            Log.d("test", String.valueOf(intent == null));
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            views.setOnClickPendingIntent(shortcutIds[i], pendingIntent);
//        }

//        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}