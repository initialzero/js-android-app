package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.content.res.Configuration;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;

import org.androidannotations.api.ViewServer;

import java.util.Locale;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class BaseActionBarActivity extends roboguice.activity.RoboActionBarActivity {
    private Locale currentLocale;

    public boolean isDevMode() {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("dev");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BugSenseWrapper.initAndStartSession(this);
        currentLocale = Locale.getDefault();
        if (isDevMode()) {
            ViewServer.get(this).addWindow(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDevMode()) {
            ViewServer.get(this).setFocusedWindow(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isDevMode()) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks change of localization
        // We are removing cookies as soon as they persist locale
        // New Basic Auth call we be triggered
        if (!currentLocale.equals(newConfig.locale)) {
            JasperMobileApplication.removeAllCookies();
            currentLocale = newConfig.locale;
        }
    }
}
