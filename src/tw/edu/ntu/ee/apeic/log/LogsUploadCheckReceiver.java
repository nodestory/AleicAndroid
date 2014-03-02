package tw.edu.ntu.ee.apeic.log;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
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

import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/1.
 */
public class LogsUploadCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ApeicUtil.APPTAG, "LogsUploadCheckReceiver onReceive.");

        File logFileFolder = new File(Environment.getExternalStorageDirectory(),
                ApeicUtil.PENDING_LOG_FILES_FOLDER);
        if (logFileFolder.exists()) {
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
            File file = new File(path);

            Log.v(ApeicUtil.APPTAG, "LogUploadIntentService onHandleIntent: start uploading " + file.getName());
        }

        private boolean upload(String path) {
            // TODO
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("upload_log");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("log_file", new FileBody(new File(path)));
            post.setEntity(builder.build());
            HttpResponse response = null;
            try {
                response = client.execute(post);
                response.getStatusLine();  // CONSIDER  Detect server complaints
                HttpEntity entity = response.getEntity();
                entity.consumeContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.getConnectionManager().shutdown();

            return false;
        }
    }
}