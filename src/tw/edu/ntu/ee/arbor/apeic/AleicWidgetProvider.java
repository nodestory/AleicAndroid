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
        Log.d(ActivityUtils.APPTAG, "Widget onEnabled.");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(ActivityUtils.APPTAG, "Widget onUpdate.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (action.equals("test")) {
            Log.d(ActivityUtils.APPTAG, "!!!");
            String name = intent.getStringExtra("name");
            if (intent != null){
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(name));
            }

        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(ActivityUtils.APPTAG, "Widget onDeleted.");
    }
}