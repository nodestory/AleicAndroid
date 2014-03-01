package tw.edu.ntu.ee.apeic.log;

import android.content.Context;

import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * Created by Linzy on 2014/2/18.
 */
public class ActivityDetecctor extends Detector {
    private ActivityRecognitionClient mActivityRecognitionClient;

    ActivityDetecctor(Context context) {
        super(context);
    }



    @Override
    void onStartRequestUpdate() {

    }
}
