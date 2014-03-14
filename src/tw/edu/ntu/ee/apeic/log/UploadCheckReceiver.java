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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/1.
 */
public class UploadCheckReceiver extends BroadcastReceiver {
    private static final String API_PREFIX = "http://140.112.170.196:8000";
    private static final String SUFFIX_UPLOAD = "upload_log";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ApeicUtil.UPLOAD_FILE_TAG, "UploadCheckReceiver onReceive");
        File logFileFolder = new File(context.getFilesDir(),
                ApeicUtil.PENDING_LOG_FILES_FOLDER);
        if (logFileFolder.exists()) {
            Log.d(ApeicUtil.UPLOAD_FILE_TAG, "Num of files to be uploaded: " +
                    String.valueOf(logFileFolder.listFiles().length));
            for (File file : logFileFolder.listFiles()) {
                Intent uploadIntent = new Intent(context, LogUploadIntentService.class);
                uploadIntent.putExtra("path", file.getAbsolutePath());
                context.startService(uploadIntent);
            }
        }
    }

    public static class LogUploadIntentService extends IntentService {

        public LogUploadIntentService() {
            super("LogUploadIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.v(ApeicUtil.UPLOAD_FILE_TAG, "LogUploadIntentService onHandleIntent");
            String path = intent.getStringExtra("path");
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() ||
                    connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                Log.d(ApeicUtil.UPLOAD_FILE_TAG, "start uploading " + path);
                upload(new File(path));
            } else {
                Log.d(ApeicUtil.UPLOAD_FILE_TAG, "postpone uploading ");
            }
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
                Log.d(ApeicUtil.UPLOAD_FILE_TAG, "Status code of uploading " + file.getName() + ": "
                        + String.valueOf(statusCode));
                if (statusCode == 200) {
                    file.delete();
                }
            } catch (IOException e) {
                Log.e(ApeicUtil.UPLOAD_FILE_TAG, e.getMessage());
            }
        }

        private String getUploadUrl() {
            return API_PREFIX + "/" + ApeicPrefsUtil.getInstance(this).getUUID() + "/" + SUFFIX_UPLOAD;
        }
    }

    public static class UploadInstalledAppsIntentService extends IntentService {

        public UploadInstalledAppsIntentService() {
            super("UploadInstalledAppsIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.v(ApeicUtil.UPLOAD_FILE_TAG, "UploadInstalledAppsIntentService onHandleIntent");

            PackageManager pm = getPackageManager();
            Set<String> currInstalledApps = new HashSet<String>();
            for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
                currInstalledApps.add(info.packageName);
            }

            ApeicPrefsUtil prefsUtil = ApeicPrefsUtil.getInstance(this);
            if (prefsUtil.getPrefs().contains(ApeicPrefsUtil.KEY_INSTALLED_APPS)) {
                Set<String> lastInstalledApps = prefsUtil.getStringSetPref(ApeicPrefsUtil.KEY_INSTALLED_APPS);
                Set<String> newlyInstalledApps = getNewlyInstalledApps(currInstalledApps, lastInstalledApps);
                Log.d(ApeicUtil.UPLOAD_FILE_TAG, "Installed Apps: ");
                for (String app : newlyInstalledApps) {
                    Log.d(ApeicUtil.UPLOAD_FILE_TAG, app);
                }
                Log.d(ApeicUtil.UPLOAD_FILE_TAG, "Removed Apps: ");
                Set<String> removedApps = getRemovedApps(currInstalledApps, lastInstalledApps);
                for (String app : removedApps) {
                    Log.d(ApeicUtil.UPLOAD_FILE_TAG, app);
                }
            } else {
                Log.d(ApeicUtil.UPLOAD_FILE_TAG, "Current installed Apps: ");
                for (String app : currInstalledApps) {
                    Log.d(ApeicUtil.UPLOAD_FILE_TAG, app);
                }
                ApeicPrefsUtil.getInstance(this).setStringSetPref(ApeicPrefsUtil.KEY_INSTALLED_APPS, currInstalledApps);
            }
        }

        private Set<String> getNewlyInstalledApps(Set<String> currApps, Set<String> prevApps) {
            Set<String> installedApps = new TreeSet<String>(currApps);
            installedApps.addAll(currApps);
            installedApps.removeAll(prevApps);
            return installedApps;
        }

        private Set<String> getRemovedApps(Set<String> currApps, Set<String> prevApps) {
            Set<String> removedApps = new TreeSet<String> (prevApps);
            removedApps.addAll(prevApps);
            removedApps.removeAll(currApps);
            return removedApps;
        }
    }
}