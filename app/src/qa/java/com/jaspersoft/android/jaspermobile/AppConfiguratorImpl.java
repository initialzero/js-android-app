package com.jaspersoft.android.jaspermobile;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;

import io.fabric.sdk.android.Fabric;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class AppConfiguratorImpl implements AppConfigurator {
    @Override
    public void configCrashAnalytics(Context appContext) {
        DefaultPrefHelper_ prefHelper = DefaultPrefHelper_.getInstance_(appContext);
        Fabric.with(appContext, new Crashlytics());
        if (prefHelper.sendCrashReports()) {
            Fabric.with(appContext, new Crashlytics());
        }
    }
}
