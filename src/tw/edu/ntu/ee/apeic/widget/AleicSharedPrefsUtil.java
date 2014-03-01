package tw.edu.ntu.ee.apeic.widget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Linzy on 2014/2/20.
 */
public class AleicSharedPrefsUtil {
    public static final String PACKAGE_NAME = "com.example.android.activityrecognition";

    // Shared preferences name
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES";

    // Keys in the repository
    public static final String KEY_LAST_ACTIVITY_TYPE = PACKAGE_NAME + ".KEY_LAST_ACTIVITY_TYPE";
    public static final String KEY_LAST_LATITUDE = PACKAGE_NAME + ".KEY_LAST_LATITUDE";
    public static final String KEY_LAST_LONGITUDE = PACKAGE_NAME + ".KEY_LAST_LONGITUDE";
    public static final String KEY_LAST_LOCATION_ACC = PACKAGE_NAME + ".KEY_LAST_LOCATION_ACC";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String getStringPref(Context context, String keyName) {
        return getSharedPreferences(context).getString(keyName , "NULL");
    }

    public static void setStringPref(Context context, String keyName, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(keyName , newValue);
        editor.commit();
    }
}