package tw.edu.ntu.ee.apeic;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Linzy on 2014/2/20.
 */
public class ApeicPrefsUtil {
    public static final String PACKAGE_NAME = "tw.edu.ntu.ee.arbor.apeic";

    // Shared preferences name
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES";

    // Keys in the repository
    // Location related
    public static final String KEY_DATE = PACKAGE_NAME + ".KEY_DATE";
    public static final String KEY_LAST_LATITUDE = PACKAGE_NAME + ".KEY_LAST_LATITUDE";
    public static final String KEY_LAST_LONGITUDE = PACKAGE_NAME + ".KEY_LAST_LONGITUDE";
    public static final String KEY_LAST_LOCATION_ACC = PACKAGE_NAME + ".KEY_LAST_LOCATION_ACC";
    public static final String KEY_LAST_SPEED = PACKAGE_NAME + ".KEY_LAST_SPEED";
    // Activity related
    public static final String KEY_LAST_ACTIVITY_TYPE = PACKAGE_NAME + ".KEY_LAST_ACTIVITY_TYPE";
    public static final String KEY_LAST_ACTIVITY_ACC = PACKAGE_NAME + ".KEY_LAST_ACTIVITY_ACC";
    public static final String KEY_LAST_APP = PACKAGE_NAME + ".KEY_LAST_APP";

    private static ApeicPrefsUtil sApeicPrefsUtilInstance = null;
    private SharedPreferences mPrefs;

    private ApeicPrefsUtil(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static ApeicPrefsUtil getInstance(Context context) {
        if (sApeicPrefsUtilInstance == null) {
            sApeicPrefsUtilInstance = new ApeicPrefsUtil(context);
        }
        return sApeicPrefsUtilInstance;
    }

    public String getStringPref(String keyName) {
        return mPrefs.getString(keyName , "NULL");
    }

    public void setStringPref(String keyName, String newValue) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(keyName , newValue);
        editor.commit();
    }

    public int getIntPref(String keyName) {
        return mPrefs.getInt(keyName, 0);
    }

    public void setIntPref(String keyName, int newValue) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(keyName, newValue);
        editor.commit();
    }

    public Long getLongPref(String keyName) {
        return mPrefs.getLong(keyName, 0);
    }

    public void setLongPref(String keyName, Long newValue) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(keyName, newValue);
        editor.commit();
    }

    public Float getFloatPref(String keyName) {
        return mPrefs.getFloat(keyName, 0);
    }

    public void setFloatPref(String keyName, Float newValue) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(keyName, newValue);
        editor.commit();
    }
}