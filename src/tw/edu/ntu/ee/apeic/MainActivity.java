/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.edu.ntu.ee.apeic;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import tw.edu.ntu.ee.apeic.log.LogFile;
import tw.edu.ntu.ee.apeic.log.LogService;
import tw.edu.ntu.ee.apeic.log.UploadCheckReceiver;
import tw.edu.ntu.ee.arbor.apeic.R;

public class MainActivity extends Activity {
    // UI elements
    private Switch mSwitch;
    private ListView mStatusListView;
    private ArrayAdapter<Spanned> mStatusAdapter;

    // For updating Lists
    private IntentFilter mBroadcastFilter;
    private LocalBroadcastManager mBroadcastManager;

    // For logging
    private ApeicUtil.REQUEST_TYPE mRequestType;
    private LogFile mLogFile;
//    private DetectionRequester mDetectionRequester;
//    private DetectionRemover mDetectionRemover;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mSwitch = (Switch) findViewById(R.id.log_service_switch);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    startLogging();
                } else {
                    stopLogging();
                }
            }
        });
        mStatusListView = (ListView) findViewById(R.id.log_listview);
        mStatusAdapter = new ArrayAdapter<Spanned>(this, R.layout.item_layout, R.id.log_text);
        mStatusListView.setAdapter(mStatusAdapter);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastFilter = new IntentFilter(ApeicUtil.ACTION_REFRESH_STATUS_LIST);
        mBroadcastFilter.addCategory(ApeicUtil.CATEGORY_LOCATION_SERVICES);

//        mDetectionRequester = new DetectionRequester(this);
//        mDetectionRemover = new DetectionRemover(this);

//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        BroadcastReceiver mReceiver = new ScreenStateUpdateReceiver();
//        TODO: remember to unregister
//        registerReceiver(mReceiver, filter);

        mLogFile = LogFile.getInstance(this);

        startUploadRoutine();
    }

    /**
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * DetectionRemover and DetectionRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ApeicUtil.CONNECTION_FAILURE_RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (ApeicUtil.REQUEST_TYPE.ADD == mRequestType) {
                        startLogging();
                    } else if (ApeicUtil.REQUEST_TYPE.REMOVE == mRequestType) {
                        stopLogging();
                    } else {
                    }
                } else {
                    Log.d(ApeicUtil.TAG, getString(R.string.no_resolution));
                }
            default:
                Log.d(ApeicUtil.TAG, getString(R.string.unknown_activity_request_code, requestCode));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBroadcastManager.registerReceiver(updateListReceiver, mBroadcastFilter);
        updateLogs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_showlog:
                updateLogs();
                return true;
            case R.id.menu_item_clearlog:
                mStatusAdapter.clear();
                mStatusAdapter.notifyDataSetChanged();

                if (!mLogFile.removeLogFiles()) {
                    Log.e(ApeicUtil.TAG, getString(R.string.log_file_deletion_error));
                } else {
                    Toast.makeText(this, R.string.logs_deleted, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBroadcastManager.unregisterReceiver(updateListReceiver);
    }

    /**
     * Respond to "Start" button by requesting activity recognition
     * updates.
     *
     * @param view The view that triggered this method.
     */
    public void onStartUpdates(View view) {
        if (!isServicesConnected()) return;

        // Set the request type. If a connection error occurs, and Google Play services can
        // handle it, then onActivityResult will use the request type to retry the request
        mRequestType = ApeicUtil.REQUEST_TYPE.ADD;

        startLogging();
//        mDetectionRequester.requestUpdates();
    }


    /**
     * Respond to "Stop" button by canceling updates.
     *
     * @param view The view that triggered this method.
     */
    public void onStopUpdates(View view) {
        if (!isServicesConnected()) return;

        // Set the request type. If a connection error occurs, and Google Play services can
        // handle it, then onActivityResult will use the request type to retry the request
        mRequestType = ApeicUtil.REQUEST_TYPE.REMOVE;

        stopLogging();
        // Pass the remove request to the remover object
//        mDetectionRemover.removeUpdates(mDetectionRequester.getActivityRequestPendingIntent());

        // Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
        // will stop the updates.
//        if (mDetectionRequester.getActivityRequestPendingIntent() != null) {
//            mDetectionRequester.getActivityPendingIntent().cancel();
//        }
    }

    private boolean isServicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
            return false;
        }
    }

    private void startLogging() {
        mRequestType = ApeicUtil.REQUEST_TYPE.ADD;
        Intent intent = new Intent(this, LogService.class);
        startService(intent);
    }

    private void stopLogging() {
        mRequestType = ApeicUtil.REQUEST_TYPE.REMOVE;
        Intent intent = new Intent(this, LogService.class);
        stopService(intent);
    }

    private void startUploadRoutine() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UploadCheckReceiver.class);
        am.setRepeating(AlarmManager.RTC, 0, ApeicUtil.LOG_FILE_UPLOAD_INTERVAL_MILLISECONDS,
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }


    private void updateLogs() {
        try {
            mStatusAdapter.clear();
            List<Spanned> appUsageLogs = mLogFile.loadLogFile();
            ListIterator iterator = appUsageLogs.listIterator(appUsageLogs.size());
            int count = 0;
            while (iterator.hasPrevious()) {
                Spanned log = (Spanned) iterator.previous();
                mStatusAdapter.add(log);
                count += 1;
                if (count == 50) {
                    break;
                }
            }
            mStatusAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            Log.e(ApeicUtil.TAG, e.getMessage(), e);
        }
    }

    /**
     * Broadcast receiver that receives activity update intents
     * It checks to see if the ListView contains items. If it
     * doesn't, it pulls in history.
     * This receiver is local only. It can't read broadcast Intents from other apps.
     */
    BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLogs();
        }
    };
}