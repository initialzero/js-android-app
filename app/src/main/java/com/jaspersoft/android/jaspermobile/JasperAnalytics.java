/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

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
