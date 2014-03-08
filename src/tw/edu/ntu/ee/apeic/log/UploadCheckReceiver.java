package tw.edu.ntu.ee.apeic.log;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
        Log.v(ApeicUtil.TAG, "UploadCheckReceiver onReceive.");

        File logFileFolder = new File(context.getFilesDir(),
                ApeicUtil.PENDING_LOG_FILES_FOLDER);
        if (logFileFolder.exists()) {
            Log.d(ApeicUtil.TAG, "Num of files to be uploaded: " +
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
            String path = intent.getStringExtra("path");
            Log.v(ApeicUtil.TAG, "LogUploadIntentService onHandleIntent: start uploading " + path);
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
                Log.d(ApeicUtil.TAG, file.getName() + " uploaded: " + String.valueOf(statusCode));
                if (statusCode == 200) {
                    file.delete();
                }
            } catch (IOException e) {
                Log.e(ApeicUtil.TAG, e.getMessage());
            }
        }

        private String getUploadUrl() {
            return API_PREFIX + "/" + ApeicPrefsUtil.getInstance(this).getUUID() + "/" + SUFFIX_UPLOAD;
        }
    }
}