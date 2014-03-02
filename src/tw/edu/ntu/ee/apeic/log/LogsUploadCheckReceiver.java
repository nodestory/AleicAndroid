package tw.edu.ntu.ee.apeic.log;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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
            Log.v(ApeicUtil.APPTAG, "LogUploadIntentService onHandleIntent: start uploading " + path);
            upload(new File(path));
        }

        private void upload(File file) {
//            Log.v(ApeicUtil.APPTAG, "LogUploadIntentService onHandleIntent: start uploading.");
//            HttpClient client = new DefaultHttpClient();
//            HttpPost post = new HttpPost("http://192.168.17.230:8080/upload_log");
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//            FileBody fb = new FileBody(file);
//            builder.addPart("file", fb);
//            final HttpEntity yourEntity = builder.build();
//            Log.v(ApeicUtil.APPTAG, file.getName());
//
//            post.setEntity(yourEntity);
//            try {
//                HttpResponse response = client.execute(post);
//            } catch (IOException e) {
//                Log.e(ApeicUtil.APPTAG, e.getMessage());
//                e.printStackTrace();
//            }
        }

    }
}