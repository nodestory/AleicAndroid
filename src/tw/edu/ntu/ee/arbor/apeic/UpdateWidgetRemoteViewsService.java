package tw.edu.ntu.ee.arbor.apeic;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.activityrecognition.ActivityUtils;
import com.example.android.activityrecognition.R;

/**
 * Created by Linzy on 2014/2/20.
 */
public class UpdateWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(ActivityUtils.APPTAG, "onGetViewFactory");
        return new GridRemoteViewsFactory(this, intent);
    }

    private class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private String[] apps;

        public GridRemoteViewsFactory(Context context, Intent intent) {
            apps = intent.getStringArrayExtra("apps");
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            Log.d(ActivityUtils.APPTAG, "onDataSetChanged");
            String temp = AleicSharedPrefsUtil.getStringPref(getApplicationContext(), "test");
            // TODO
            apps = temp.split(" ");
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return apps.length;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            ApplicationInfo info = getApplicationInfo(apps[i]);
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.item_widget);
            remoteViews.setImageViewBitmap(R.id.imageView, getApplicationIcon(info));
            remoteViews.setTextViewText(R.id.textView, getApplicationName(info));

//            Intent startActivityIntent = getPackageManager().getLaunchIntentForPackage(apps[i]);
//            PendingIntent pendingIntent = PendingIntent.getActivity(
//                    getApplicationContext(), 0, startActivityIntent, 0);
//            remoteViews.setOnClickPendingIntent(R.id.gridView, pendingIntent);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("name", apps[i]);
            remoteViews.setOnClickFillInIntent(R.id.layout, fillInIntent);


            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private ApplicationInfo getApplicationInfo(String packageName) {
            try {
                return getPackageManager().getApplicationInfo(packageName, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                return null;
            }
        }

        private String getApplicationName(ApplicationInfo info) {
            // TODO
            return ((info != null) ? (String) getPackageManager().getApplicationLabel(info) : "???");
        }

        private Bitmap getApplicationIcon(ApplicationInfo info) {
            // TODO
            Drawable icon = getPackageManager().getApplicationIcon(info);
            return ((BitmapDrawable) icon).getBitmap();
        }
    }
}