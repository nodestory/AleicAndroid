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

package tw.edu.ntu.ee.apeic.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tw.edu.ntu.ee.arbor.apeic.R;

/**
 * Utility class that handles writing, reading and cleaning up files where we
 * log the activities that where detected by the activity detection service.
 */
public class LogFile {
    private final Context mContext;
    private SharedPreferences mPrefs;

    private File mLogFileFolder;
    private File mLogFile;
    private String mFileName;
    private int mFileNumber;

    private PrintWriter mLogWriter;

    // Store an sLogFileInstance of the log file
    private static LogFile sLogFileInstance = null;

    /**
     * Singleton that ensures that only one sLogFileInstance of the LogFile exists at any time
     *
     * @param context A Context for the current app
     */
    private LogFile(Context context) {
        mContext = context;
        mPrefs = context.getSharedPreferences(ActivityUtils.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        if (!mPrefs.contains(ActivityUtils.KEY_LOG_FILE_NUMBER)) {
            mFileNumber = 1;
        } else {
            int fileNum = mPrefs.getInt(ActivityUtils.KEY_LOG_FILE_NUMBER, 0);
            mFileNumber = fileNum + 1;
        }

        String dateString = new SimpleDateFormat("yyyy_MM_dd", Locale.TAIWAN).format(new Date());
        mFileName = context.getString(
                R.string.log_filename,
                ActivityUtils.LOG_FILE_NAME_PREFIX,
                dateString,
                mFileNumber++,
                ActivityUtils.LOG_FILE_NAME_SUFFIX);
        mLogFileFolder = new File(Environment.getExternalStorageDirectory(), ActivityUtils.LOG_FILE_FOLDER);
        if (!mLogFileFolder.exists()) {
            mLogFileFolder.mkdir();
        }

        Editor editor = mPrefs.edit();
        editor.putInt(ActivityUtils.KEY_LOG_FILE_NUMBER, mFileNumber);
        editor.putString(ActivityUtils.KEY_LOG_FILE_NAME, mFileName);
        editor.commit();

        mLogFile = createLogFile(mFileName);
    }

    /**
     * Create an sLogFileInstance of log file, or return the current sLogFileInstance
     *
     * @param context A Context for the current app
     * @return An sLogFileInstance of this class
     */
    public static LogFile getInstance(Context context) {
        if (sLogFileInstance == null) {
            sLogFileInstance = new LogFile(context);
        }
        return sLogFileInstance;
    }

    private void initLogWriter() {
        try {
            if (mLogWriter != null) {
                mLogWriter.close();
            }
            mLogWriter = new PrintWriter(new FileWriter(mLogFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createLogFile(String filename) {
        File newFile = new File(mLogFileFolder, filename);
        Log.d(ActivityUtils.APPTAG, newFile.getAbsolutePath() + " : " + newFile.getName());
        return newFile;
    }

    public boolean removeLogFiles() {
        boolean removed = true;
        if (mLogFileFolder.exists()) {
            for (File file : mLogFileFolder.listFiles()) {
                if (!file.delete()) {
                    Log.e(ActivityUtils.APPTAG, file.getAbsolutePath() + " : " + file.getName());
                    removed = false;
                }
            }
        }
        return removed;
    }

    public void log(String message) {
        initLogWriter();
        mLogWriter.println(message);
        mLogWriter.flush();
    }

    public List<Spanned> loadLogFile() throws IOException {
        List<Spanned> content = new ArrayList<Spanned>();
        if (!mLogFile.exists()) {
            return content;
        }

        BufferedReader reader = new BufferedReader(new FileReader(mLogFile));
        String line;

        while ((line = reader.readLine()) != null) {
            content.add(new SpannedString(line));
        }
        reader.close();
        return content;
    }
}