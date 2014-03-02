package tw.edu.ntu.ee.apeic.log;

import android.app.IntentService;
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

import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/2.
 */
public class UploadLogFileService extends IntentService {

    public UploadLogFileService() {
        super("UploadLogFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String path = (String) intent.getCharSequenceExtra("path");
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://192.168.17.230:8080/upload_log");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("log_file", new FileBody(new File(path)));
        post.setEntity(builder.build());
        HttpResponse response = null;
        try {
            response = client.execute(post);
            response.getStatusLine();  // CONSIDER  Detect server complaints
            Log.d(ApeicUtil.APPTAG, String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity entity = response.getEntity();
            entity.consumeContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.getConnectionManager().shutdown();
    }
}
