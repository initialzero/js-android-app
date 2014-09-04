package com.jaspersoft.android.jaspermobile.test;


import android.app.Activity;

import com.jaspersoft.android.jaspermobile.R;


public final class NameUtils {

    private final String mPrefix;

    public NameUtils(String prefix) {
        mPrefix = prefix;
    }

    public String generateName(Activity activity, String feature) {
        return String.format("%s_%s_%s_%s", mPrefix, feature,
                getDeviceName(activity), getPerspective(activity));
    }

    private String getPerspective(Activity activity) {
        int height = activity.getWindow().getDecorView().getHeight();
        int width = activity.getWindow().getDecorView().getWidth();

        if (height > width) {
            return "port";
        } else {
            return "land";
        }
    }

    private String getDeviceName(Activity activity) {
        return activity.getResources().getString(R.string.device);
    }

}
