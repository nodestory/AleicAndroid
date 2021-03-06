package tw.edu.ntu.ee.apeic;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Linzy on 2014/2/20.
 */
public class ApeicPrefsUtil {

    // Shared preferences name
    public static final String SHARED_PREFERENCES_NAME = ApeicUtil.PACKAGE_NAME + ".SHARED_PREFERENCES";

    // Keys in the repository
    public static final String KEY_IS_LOGGING = ApeicUtil.PACKAGE_NAME + ".IS_LOGGING";
    public static final String KEY_INSTALLED_APPS = ApeicUtil.PACKAGE_NAME + ".KEY_INSTALLED_APPS";
    public static final String KEY_REGISTERING_APPS = ApeicUtil.PACKAGE_NAME + ".KEY_REGISTERING_APPS";
    public static final String KEY_UNREGISTERING_APPS = ApeicUtil.PACKAGE_NAME + ".KEY_UNREGISTERING_APPS";
    // Location related
    public static final String KEY_LOG_FILE_NUMBER = ApeicUtil.PACKAGE_NAME + ".LOG_FILE_NUMBER";
    public static final String KEY_ANDROID_ID = ApeicUtil.PACKAGE_NAME + ".KEY_ANDROID_ID";
    public static final String KEY_DATE = ApeicUtil.PACKAGE_NAME + ".KEY_DATE";
    public static final String KEY_LAST_LATITUDE = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_LATITUDE";
    public static final String KEY_LAST_LONGITUDE = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_LONGITUDE";
    public static final String KEY_LAST_LOCATION_ACC = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_LOCATION_ACC";
    public static final String KEY_LAST_SPEED = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_SPEED";
    // Activity related
    public static final String KEY_LAST_ACTIVITY_TYPE = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_ACTIVITY_TYPE";
    public static final String KEY_LAST_ACTIVITY_ACC = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_ACTIVITY_ACC";
    // Illumination related
    public static final String KEY_ILLUMINATION = ApeicUtil.PACKAGE_NAME + ".KEY_ILLUMINATION";
    public static final String KEY_MAX_ILLUMINATION = ApeicUtil.PACKAGE_NAME + ".KEY_MAX_ILLUMINATION";
    // Application related
    public static final String KEY_LAST_APP = ApeicUtil.PACKAGE_NAME + ".KEY_LAST_APP";

    private static ApeicPrefsUtil sApeicPrefsUtilInstance = null;
    private Context mContext;
    private SharedPreferences mPrefs;

    private ApeicPrefsUtil(Context context) {
        mContext = context;
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static ApeicPrefsUtil getInstance(Context context) {
        if (sApeicPrefsUtilInstance == null) {
            sApeicPrefsUtilInstance = new ApeicPrefsUtil(context);
        }
        return sApeicPrefsUtilInstance;
    }

    public SharedPreferences getPrefs() {
        return mPrefs;
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

    public boolean getBooleanPref(String keyName) {
        return mPrefs.getBoolean(keyName, false);
    }

    public void setBooleanPref(String keyName, boolean newValue) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(keyName, newValue);
        editor.commit();
    }

    public Set<String> getStringSetPref(String keyName) {
        return mPrefs.getStringSet(keyName, new HashSet<String>());
    }

    public void setStringSetPref(String keyName, Set<String> newValue) {
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putStringSet(keyName, newValue);
        editor.commit();
    }

    public String getAndroidID() {
        if (!mPrefs.contains(KEY_ANDROID_ID)) {
            String id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            setStringPref(KEY_ANDROID_ID, id);
        }
        return getStringPref(KEY_ANDROID_ID);
    }
}