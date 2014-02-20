package tw.edu.ntu.ee.arbor.apeic;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
        Log.d(ActivityUtils.APPTAG, "AleicWidgetProvider onEnabled.");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.v(ActivityUtils.APPTAG, "AleicWidgetProvider onDeleted.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ActivityUtils.APPTAG, "AleicWidgetProvider onReceive.");

        String action = intent.getAction();
        if (action.equals(ActivityUtils.ACTION_ITEM_CLICKED)) {
            startActivity(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(ActivityUtils.APPTAG, "AleicWidgetProvider onUpdate.");
    }

    private void startActivity(Context context, Intent intent) {
        String packageName = intent.getStringExtra("packageName");
        if (packageName != null) {
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
        }
    }
}