package com.example.android.activityrecognition;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by Linzy on 2014/2/9.
 */
public class PhoneStateDetector {
    public static final String STATE_IDLE = "IDLE";
    public static final String STATE_OFFHOOK = "OFFHOOK";
    public static final String STATE_RINGING = "RINGING";

    private String mState;

    public PhoneStateDetector(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                String mState = "N/A";
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        mState = STATE_IDLE;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        mState = STATE_OFFHOOK;
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        mState = STATE_RINGING;
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public String getLog() {
        return mState;
    }
}
