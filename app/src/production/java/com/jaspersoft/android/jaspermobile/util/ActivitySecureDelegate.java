package com.jaspersoft.android.jaspermobile.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ActivitySecureDelegate {

    private Activity mActivity;

    private ActivitySecureDelegate() {
    }

    public static ActivitySecureDelegate create(Activity activity) {
        ActivitySecureDelegate activitySecureDelegate = new ActivitySecureDelegate();
        activitySecureDelegate.mActivity = activity;
        return activitySecureDelegate;
    }
    public void onCreate(Bundle savedInstanceState){
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }
}
