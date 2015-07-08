package com.jaspersoft.android.jaspermobile;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperAnalytics implements Analytics {

    private Tracker tracker;

    @Override
    public void init(Context appContext) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(appContext);
        tracker = analytics.newTracker(R.xml.analytics_tracker);
    }

    @Override
    public void trackPrintEvent() {
        checkTracker();
        tracker.setScreenName("PrintScreen");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void checkTracker() {
        if (tracker == null) {
            throw new IllegalStateException("Analytics tracker has not been initialized");
        }
    }
}
