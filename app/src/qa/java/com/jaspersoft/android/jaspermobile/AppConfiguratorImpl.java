package com.jaspersoft.android.jaspermobile;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class AppConfiguratorImpl implements AppConfigurator {
    @Override
    public void configCrashAnalytics(Context appContext) {
        Fabric.with(appContext, new Crashlytics());
        if (prefHelper.sendCrashReports()) {
            Fabric.with(appContext, new Crashlytics());
        }
    }
}
