package tw.edu.ntu.ee.apeic.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import tw.edu.ntu.ee.apeic.ApeicUtil;

/**
 * Created by Linzy on 2014/3/7.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(ApeicUtil.TAG, "" + "BootCompletedReceiver onReceive.");

        Intent requestUpdatesIntent = new Intent(context, LogService.class);
        context.startService(requestUpdatesIntent);
    }
}