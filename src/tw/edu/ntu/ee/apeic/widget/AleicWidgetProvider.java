package tw.edu.ntu.ee.apeic.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/2/10.
 */
public class AleicWidgetProvider extends AppWidgetProvider {
    private PendingIntent mUpdatePendingIntent;

    @Override
    public void onEnabled(Context context) {
        Log.d(ApeicUtil.APPTAG, "AleicWidgetProvider onEnabled.");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.v(ApeicUtil.APPTAG, "AleicWidgetProvider onDeleted.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ApeicUtil.APPTAG, "AleicWidgetProvider onReceive.");

        String action = intent.getAction();
        if (action.equals(ApeicUtil.ACTION_ITEM_CLICKED)) {
            startActivity(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(ApeicUtil.APPTAG, "AleicWidgetProvider onUpdate.");
    }

    private void startActivity(Context context, Intent intent) {
        String packageName = intent.getStringExtra("packageName");
        if (packageName != null) {
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
        }
    }
}