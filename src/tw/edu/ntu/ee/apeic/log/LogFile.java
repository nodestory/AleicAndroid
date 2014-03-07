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

import tw.edu.ntu.ee.apeic.ApeicPrefsUtil;
import tw.edu.ntu.ee.apeic.ApeicUtil;
import tw.edu.ntu.ee.arbor.apeic.R;

/**
 * Utility class that handles writing, reading and cleaning up files where we
 * log the activities that where detected by the activity detection service.
 */
public class LogFile {
    private final Context mContext;

    private File mPendingLogsFileFolder;
    private File mLogFileFolder;
    private File mLogFile;

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
        mPendingLogsFileFolder = createLogFolder(ApeicUtil.PENDING_LOG_FILES_FOLDER);
        mLogFileFolder = createLogFolder(ApeicUtil.LOG_FILE_FOLDER);
        mLogFile = createLogFile();
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

    private File createLogFile() {
        File newFile = new File(mLogFileFolder, createFileName());
        return newFile;
    }

    private String createFileName() {
        String dateString = new SimpleDateFormat("yyyy_MM_dd", Locale.TAIWAN).format(new Date());
        String lastDateString = ApeicPrefsUtil.getInstance(mContext).getStringPref(ApeicPrefsUtil.KEY_DATE);
        int fileNumber = (dateString != lastDateString) ?
                1 : ApeicPrefsUtil.getInstance(mContext).getIntPref(ApeicPrefsUtil.KEY_LOG_FILE_NUMBER) + 1;
        ApeicPrefsUtil.getInstance(mContext).setIntPref(ApeicPrefsUtil.KEY_LOG_FILE_NUMBER, fileNumber);

        String fileName = mContext.getString(
                R.string.log_filename,
                ApeicUtil.LOG_FILE_NAME_PREFIX,
                dateString,
                fileNumber,
                ApeicUtil.LOG_FILE_NAME_SUFFIX);

        return fileName;
    }

    private File createLogFolder(String name) {
        File folder = new File(mContext.getFilesDir(), name);
//        File folder = new File(Environment.getExternalStorageDirectory(), name);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

    public boolean removeLogFiles() {
        boolean removed = true;
        if (mLogFileFolder.exists()) {
            for (File file : mLogFileFolder.listFiles()) {
                if (!file.delete()) {
                    Log.e(ApeicUtil.TAG, file.getAbsolutePath() + " : " + file.getName());
                    removed = false;
                }
            }
        }

        if (mPendingLogsFileFolder.exists()) {
            for (File file : mPendingLogsFileFolder.listFiles()) {
                Log.d(ApeicUtil.TAG, file.getName());
                if (!file.delete()) {
                    Log.e(ApeicUtil.TAG, file.getAbsolutePath() + " : " + file.getName());
                    removed = false;
                }
            }
        }
        return removed;
    }

    public void write(String message) {
        Log.d(ApeicUtil.TAG, mLogFile.getAbsolutePath());
        Log.d(ApeicUtil.TAG, String.valueOf(mLogFile.length()));
        initLogWriter();
        mLogWriter.println(message);
        mLogWriter.flush();
    }

    private void initLogWriter() {
        try {
            if (mLogWriter != null) {
                mLogWriter.close();
            }

            if (shouldCreateNewFile()) {
                for (File file : mLogFileFolder.listFiles()) {
                    file.renameTo(new File(mPendingLogsFileFolder, file.getName()));
                }
                mLogFile = createLogFile();
            }
            mLogWriter = new PrintWriter(new FileWriter(mLogFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldCreateNewFile() {
        String dateString = new SimpleDateFormat("yyyy_MM_dd", Locale.TAIWAN).format(new Date());
        String lastDateString = ApeicPrefsUtil.getInstance(mContext).getStringPref(ApeicPrefsUtil.KEY_DATE);
        if (lastDateString == "NULL") {
            ApeicPrefsUtil.getInstance(mContext).setStringPref(ApeicPrefsUtil.KEY_DATE, dateString);
            return true;
        }
        return !dateString.equals(lastDateString);
//        return mLogFile.length() > ApeicUtil.MAX_FILE_SIZE;
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