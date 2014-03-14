package tw.edu.ntu.ee.apeic.log;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/1.
 */
public class UploadCheckReceiver extends BroadcastReceiver {
    private static final String API_PREFIX = "http://140.112.170.196:8000";
    private static final String SUFFIX_UPLOAD = "upload_log";
    private static final String SUFFIX_REGISTER = "register_app";
    private static final String SUFFIX_UNREGISTER = "unregister_app";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ApeicUtil.TAG_HTTP, "UploadCheckReceiver onReceive");

        ConnectivityManager connManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() &&
                !connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            return;
        }

        File logFileFolder = new File(context.getFilesDir(),
                ApeicUtil.PENDING_LOG_FILES_FOLDER);
        if (logFileFolder.exists()) {
            Log.d(ApeicUtil.TAG_HTTP, "Num of files to be uploaded: " +
                    String.valueOf(logFileFolder.listFiles().length));
            for (File file : logFileFolder.listFiles()) {
                Intent uploadIntent = new Intent(context, LogUploadIntentService.class);
                uploadIntent.putExtra("path", file.getAbsolutePath());
                context.startService(uploadIntent);
            }
        }

        Intent updateIntent = new Intent(context, UpdateInstalledAppsIntentService.class);
        context.startService(updateIntent);
    }

    public static class LogUploadIntentService extends IntentService {

        public LogUploadIntentService() {
            super("LogUploadIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.v(ApeicUtil.TAG_HTTP, "LogUploadIntentService onHandleIntent");
            String path = intent.getStringExtra("path");
            upload(new File(path));
        }

        private void upload(File file) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(getUploadUrl());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody fb = new FileBody(file);
            builder.addPart("log_file", fb);
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            try {
                HttpResponse response = client.execute(post);
                int statusCode = response.getStatusLine().getStatusCode();
                Log.d(ApeicUtil.TAG_HTTP, "Status code of uploading " + file.getName() + ": "
                        + String.valueOf(statusCode));
                if (statusCode == 200) {
                    file.delete();
                }
            } catch (IOException e) {
                Log.e(ApeicUtil.TAG_HTTP, e.getMessage());
            }
        }

        private String getUploadUrl() {
            return API_PREFIX + "/" + ApeicPrefsUtil.getInstance(this).getAndroidID() + "/" + SUFFIX_UPLOAD;
        }
    }

    public static class UpdateInstalledAppsIntentService extends IntentService {

        public UpdateInstalledAppsIntentService() {
            super("UpdateInstalledAppsIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.v(ApeicUtil.TAG_HTTP, "UpdateInstalledAppsIntentService onHandleIntent");

            PackageManager pm = getPackageManager();
            Set<String> currInstalledApps = new HashSet<String>();
            for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
                if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                    currInstalledApps.add(info.packageName);
                }
            }

            ApeicPrefsUtil prefsUtil = ApeicPrefsUtil.getInstance(this);
            Set<String> registeringApps = prefsUtil.getStringSetPref(ApeicPrefsUtil.KEY_REGISTERING_APPS);
            if (registeringApps.size() > 0) {
                Log.v(ApeicUtil.TAG_HTTP, "Re-update newly installed apps.");
                registerApps(prefsUtil.getStringSetPref(ApeicPrefsUtil.KEY_REGISTERING_APPS));
            }
            Set<String> unregisteringApps = prefsUtil.getStringSetPref(ApeicPrefsUtil.KEY_REGISTERING_APPS);
            if (unregisteringApps.size() > 0) {
                Log.v(ApeicUtil.TAG_HTTP, "Re-update uninstalled installed apps.");
                unregisterApps(prefsUtil.getStringSetPref(ApeicPrefsUtil.KEY_UNREGISTERING_APPS));
            }

            if (prefsUtil.getPrefs().contains(ApeicPrefsUtil.KEY_INSTALLED_APPS)) {
                Set<String> lastInstalledApps = prefsUtil.getStringSetPref(ApeicPrefsUtil.KEY_INSTALLED_APPS);
                Set<String> newlyInstalledApps = getNewlyInstalledApps(currInstalledApps, lastInstalledApps);
                if (newlyInstalledApps.size() > 0) {
                    Log.v(ApeicUtil.TAG_HTTP, "Newly installed Apps: ");
                    registerApps(newlyInstalledApps);
                }

                Set<String> removedApps = getUninstalledApps(currInstalledApps, lastInstalledApps);
                if (removedApps.size() > 0) {
                    Log.v(ApeicUtil.TAG_HTTP, "Uninstalled Apps: ");
                    unregisterApps(removedApps);
                }
            } else {
                Log.v(ApeicUtil.TAG_HTTP, "Current installed Apps: ");
                registerApps(currInstalledApps);
            }

            ApeicPrefsUtil.getInstance(this).setStringSetPref(
                    ApeicPrefsUtil.KEY_INSTALLED_APPS, currInstalledApps);
        }

        private Set<String> getNewlyInstalledApps(Set<String> currApps, Set<String> prevApps) {
            Set<String> apps = new HashSet<String>();
            for (String app : currApps) {
                if (!prevApps.contains(app)) {
                    apps.add(app);
                }
            }
            return apps;
        }

        private Set<String> getUninstalledApps(Set<String> currApps, Set<String> prevApps) {
            Set<String> apps = new HashSet<String>();
            for (String app : prevApps) {
                if (!currApps.contains(app)) {
                    apps.add(app);
                }
            }
            return apps;
        }

        private void registerApps(Set<String> apps) {
            Set<String> registeringApps = ApeicPrefsUtil.getInstance(this).
                    getStringSetPref(ApeicPrefsUtil.KEY_REGISTERING_APPS);
            for (String app : apps) {
                Log.d(ApeicUtil.TAG, app);
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(API_PREFIX + "/" + ApeicPrefsUtil.getInstance(this).getAndroidID()
                        + "/" + SUFFIX_REGISTER + "?app=" + app);
                try {
                    HttpResponse response = client.execute(get);
                    int statusCode = response.getStatusLine().getStatusCode();
                    Log.d(ApeicUtil.TAG_HTTP, "Status code of registering " + app + ": "
                            + String.valueOf(statusCode));
                    if (statusCode == 200) {
                        if (registeringApps.contains(app)) {
                            registeringApps.remove(app);
                        }
                    } else {
                        registeringApps.add(app);
                    }
                } catch (IOException e) {
                    Log.e(ApeicUtil.TAG_HTTP, e.getMessage());
                    registeringApps.add(app);
                }
            }
            ApeicPrefsUtil.getInstance(this).setStringSetPref(
                    ApeicPrefsUtil.KEY_REGISTERING_APPS, registeringApps);
        }

        private void unregisterApps(Set<String> apps) {
            Set<String> unregisteringApps = ApeicPrefsUtil.getInstance(this).
                    getStringSetPref(ApeicPrefsUtil.KEY_UNREGISTERING_APPS);
            for (String app : apps) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(API_PREFIX + "/" + ApeicPrefsUtil.getInstance(this).getAndroidID()
                        + "/" + SUFFIX_UNREGISTER + "?app=" + app);
                try {
                    HttpResponse response = client.execute(get);
                    int statusCode = response.getStatusLine().getStatusCode();
                    Log.d(ApeicUtil.TAG_HTTP, "Status code of unregistering " + app + ": "
                            + String.valueOf(statusCode));
                    if (statusCode == 200) {
                        if (unregisteringApps.contains(app)) {
                            unregisteringApps.remove(app);
                        }
                    } else {
                        unregisteringApps.add(app);
                    }
                } catch (IOException e) {
                    Log.e(ApeicUtil.TAG_HTTP, e.getMessage());
                    unregisteringApps.add(app);
                }
            }
            ApeicPrefsUtil.getInstance(this).setStringSetPref(
                    ApeicPrefsUtil.KEY_UNREGISTERING_APPS, unregisteringApps);
        }
    }
}