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


import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboCastActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params.ReportParamsSerializer;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.cast.ReportCastHelper;
import com.jaspersoft.android.jaspermobile.util.cast.ReportPresentation;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EActivity(R.layout.activity_cast_report)
public class ReportCastActivity extends RoboCastActivity implements ReportPresentation.ReportCastListener {

    @Extra
    protected ResourceLookup resource;

    @Bean
    protected ReportCastHelper reportCastHelper;

    private ReportPresentation reportPresentation;

    @ViewById(R.id.reportScroll)
    protected ScrollView reportScroll;

    @ViewById(R.id.reportScrollPosition)
    protected View reportScrollPosition;

    @Inject
    protected ReportParamsStorage paramsStorage;
    @Inject
    protected ReportParamsSerializer paramsSerializer;

    @AfterInject
    protected void init() {
        reportPresentation = reportCastHelper.getReportPresentation();
    }

    @AfterViews
    protected void run() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resource.getLabel());
        }

        reportScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                reportPresentation.scrollTo(calculateScrollPercent());
            }
        });

        if (reportPresentation != null) {
            reportPresentation.setReportCastListener(this);
            if (reportPresentation.isInitialized() && !reportPresentation.isCastingReport()) {
                requestReportCasting();
            }
        } else {
            ProgressDialogFragment.builder(getSupportFragmentManager())
                    .setLoadingMessage(R.string.loading_msg)
                    .show();
        }
    }

    @Override
    public void onPresentationStarted() {
        super.onPresentationStarted();

        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.r_pd_initializing_msg)
                .show();

        reportPresentation = reportCastHelper.getReportPresentation();
        reportPresentation.setReportCastListener(this);
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
            reportPresentation.stopReportCasting();
        }
    }

    @Override
    public void onPresentationInitialized() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        requestReportCasting();
    }

    @Override
    public void onReportShown(float contentScale) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        reportScrollPosition.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) (reportScrollPosition.getHeight() * contentScale)));
    }

    @Override
    public void onError(String error) {

    }

    private float calculateScrollPercent() {
        return reportScroll.getScrollY() / (float) (reportScrollPosition.getHeight() - reportScroll.getHeight());
    }

    private void requestReportCasting() {
        reportPresentation.castReport(resource.getUri(), paramsSerializer.toJson(getReportParameters()));
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.r_pd_running_report_msg)
                .show();
    }

    private List<ReportParameter> getReportParameters() {
        return paramsStorage.getInputControlHolder(resource.getUri()).getReportParams();
    }
}
