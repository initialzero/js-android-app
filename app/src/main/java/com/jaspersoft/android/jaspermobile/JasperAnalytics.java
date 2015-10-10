package com.jaspersoft.android.jaspermobile;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperAnalytics implements Analytics {

    private static final String TRACKER_ID_KEY = "&tid";

    private static final String CLICK_LABEL = "User click";
    private static final String PRINT_CATEGORY = "Print";

    private Tracker tracker;

    @Override
    public void init(Application appContext) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(appContext);
        analytics.enableAutoActivityReports(appContext);

        tracker = analytics.newTracker(R.xml.analytics_tracker);
        tracker.set(TRACKER_ID_KEY, appContext.getString(R.string.google_analytics_tracking_id));
    }

    @Override
    public void trackPrintEvent(PrintType printType) {
        checkTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(PRINT_CATEGORY)
                .setAction(printType.name())
                .setLabel(CLICK_LABEL)
                .build());
    }

    private void checkTracker() {
        if (tracker == null) {
            throw new IllegalStateException("Analytics tracker has not been initialized");
        }
    }
}
