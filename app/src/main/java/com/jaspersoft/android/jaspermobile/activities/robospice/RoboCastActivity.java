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

package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.cast.ReportCastHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EActivity
@OptionsMenu(R.menu.cast_menu)
public class RoboCastActivity extends RoboToolbarActivity implements ReportCastHelper.CastServiceCallbacks {

    @Bean
    protected ReportCastHelper mReportCastHelper;

    @OptionsMenuItem(R.id.castReport)
    protected MenuItem castAction;

    private ReportCastHelper.BaseMediaRouterCallback mediaRouterCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReportCastHelper.setRouteSelectListener(this);
        mediaRouterCallback = mReportCastHelper.createCallback();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        if (castAction != null) {
            MediaRouteActionProvider mediaRouteActionProvider =
                    (MediaRouteActionProvider) MenuItemCompat.getActionProvider(castAction);
            mReportCastHelper.applyRouteSelector(mediaRouteActionProvider);
            return true;
        }
        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReportCastHelper.registerCallback(mediaRouterCallback);
    }

    @Override
    protected void onStop() {
        mReportCastHelper.unregisterCallback(mediaRouterCallback);
        super.onStop();
    }

    @Override
    public void onRouteSelected() {
        mReportCastHelper.startCastService(RoboCastActivity.this);
    }

    @Override
    public void onPresentationStarted() {

    }

    @Override
    public void onPresentationStopped() {

    }
}
