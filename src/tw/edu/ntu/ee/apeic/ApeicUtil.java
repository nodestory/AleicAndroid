package tw.edu.ntu.ee.apeic;

public final class ApeicUtil {

    public static final String TAG = "Apeic";

    public static final String PACKAGE_NAME = "tw.edu.ntu.ee.apeic";

    // Types of logging requests
    public enum REQUEST_TYPE {ADD, REMOVE}

    // Request codes
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Intent actions and extras for sending information from the IntentService to the Activity
    public static final String ACTION_CONNECTION_ERROR = PACKAGE_NAME + ".ACTION_CONNECTION_ERROR";
    public static final String ACTION_ITEM_CLICKED = PACKAGE_NAME + ".action.ITEM_CLICKED";
    public static final String ACTION_REFRESH_STATUS_LIST = PACKAGE_NAME + ".ACTION_REFRESH_STATUS_LIST";
    public static final String CATEGORY_LOCATION_SERVICES = PACKAGE_NAME + ".CATEGORY_LOCATION_SERVICES";
    public static final String EXTRA_CONNECTION_ERROR_CODE = PACKAGE_NAME + ".EXTRA_CONNECTION_ERROR_CODE";
    public static final String EXTRA_CONNECTION_ERROR_MESSAGE = PACKAGE_NAME + ".EXTRA_CONNECTION_ERROR_MESSAGE";

    public static final int MAX_FILE_SIZE = 10000;
//    public static final int MAX_FILE_SIZE = 1000;

    // Constants used to establish the interval of each update
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_MILLISECONDS = 10*MILLISECONDS_PER_SECOND;
    public static final int LOG_FILE_UPLOAD_INTERVAL_MILLISECONDS = 6*60*60*MILLISECONDS_PER_SECOND;
//    public static final int LOG_FILE_UPLOAD_INTERVAL_MILLISECONDS = 60*MILLISECONDS_PER_SECOND;
    public static final int WIDGET_UPDATE_INTERVAL_MILLISECONDS = 10*MILLISECONDS_PER_SECOND;

    // Constants for constructing the log file name
    public static final String LOG_FILE_NAME_PREFIX = "app_usage";
    public static final String LOG_FILE_NAME_SUFFIX = ".log";
    public static final String LOG_FILE_FOLDER = "apeic";
    public static final String PENDING_LOG_FILES_FOLDER = LOG_FILE_FOLDER + "/pending";
}