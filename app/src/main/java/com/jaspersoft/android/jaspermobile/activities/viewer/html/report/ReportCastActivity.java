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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.content.DialogInterface;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboCastActivity;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.util.cast.ReportCastHelper;
import com.jaspersoft.android.jaspermobile.util.cast.ReportPresentation;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EActivity(R.layout.activity_cast_report)
public class ReportCastActivity extends RoboCastActivity {

    @Extra
    protected ResourceLookup resource;

    @Bean
    protected ReportCastHelper reportCastHelper;

    private ReportPresentation reportPresentation;

    @AfterInject
    protected void init() {
        reportPresentation = reportCastHelper.getReportPresentation();
    }

    @AfterViews
    protected void run() {
        if (reportPresentation == null) {
            ProgressDialogFragment.builder(getSupportFragmentManager())
                    .setLoadingMessage(R.string.loading_msg)
                    .show();
        } else if (!reportPresentation.isReportRunning()) {
            reportPresentation.runReport(resource);
        }
    }

    @Override
    public void onPresentationStarted() {
        super.onPresentationStarted();

        reportPresentation = reportCastHelper.getReportPresentation();
        reportPresentation.runReport(resource);

        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @Override
    public void onPresentationStopped() {
        super.onPresentationStopped();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (reportPresentation != null && !reportPresentation.isShowing()) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (reportPresentation.isShowing()) {
            reportPresentation.stopRunning();
        }
    }
}
