/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperAnalytics implements Analytics {

    private static final String SERVER_VERSION_KEY = "&cd1";
    private static final String SERVER_EDITION_KEY = "&cd2";

    private Tracker mTracker;

    @Override
    public void init(Application appContext) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(appContext);

        mTracker = analytics.newTracker(appContext.getString(R.string.google_analytics_tracking_id));
        mTracker.setSessionTimeout(900);
    }

    @Override
    public void setScreenName(String screenName) {
        checkTracker();

        mTracker.setScreenName(screenName);
    }

    @Override
    public void sendScreenView(String screenName, List<Dimension> dimensions) {
        checkTracker();
        setScreenName(screenName);

        HitBuilders.ScreenViewBuilder screenHitBuilder = new HitBuilders.ScreenViewBuilder();

        if (dimensions != null) {
            for (Dimension dimension : dimensions) {
                screenHitBuilder.setCustomDimension(dimension.getKey(), dimension.getValue());
            }
        }

        mTracker.send(screenHitBuilder.build());
    }

    @Override
    public void sendEvent(String eventCategory, String eventAction, String eventLabel) {
        checkTracker();

        HitBuilders.EventBuilder eventHitBuilder = new HitBuilders.EventBuilder();
        eventHitBuilder.setCategory(eventCategory);
        eventHitBuilder.setAction(eventAction);
        if (eventLabel != null) {
            eventHitBuilder.setLabel(eventLabel);
        }

        mTracker.send(eventHitBuilder.build());
    }

    @Override
    public void sendUserChangedEvent() {
        checkTracker();

        mTracker.send(new HitBuilders.EventBuilder()
                .setNewSession()
                .setCategory(EventCategory.ACCOUNT.getValue())
                .setAction(EventAction.CHANGED.getValue())
                .build());
    }

    @Override
    public void setServerInfo(String serverVersion, String serverEdition) {
        checkTracker();

        mTracker.set(SERVER_VERSION_KEY, serverVersion);
        mTracker.set(SERVER_EDITION_KEY, serverEdition);
    }

    private void checkTracker() {
        if (mTracker == null) {
            throw new IllegalStateException("Analytics mTracker has not been initialized");
        }
    }
}
