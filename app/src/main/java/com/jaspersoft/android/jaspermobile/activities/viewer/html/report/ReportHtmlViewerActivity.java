/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.FilterManagerFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.FilterManagerFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.PaginationManagerFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.PaginationManagerFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.ReportActionFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.ReportActionFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.ReportExecutionFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.ReportExecutionFragment_;
import com.jaspersoft.android.jaspermobile.info.ServerInfoManager;
import com.jaspersoft.android.jaspermobile.info.ServerInfoSnapshot;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportDataResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

/**
 * Activity that performs report viewing in HTML format.
 *
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.4
 */
@EActivity(R.layout.report_viewer_layout)
public class ReportHtmlViewerActivity extends RoboSpiceFragmentActivity {

    // Extras
    public static final String EXTRA_REPORT_PARAMETERS = "ReportHtmlViewerActivity.EXTRA_REPORT_PARAMETERS";
    public static final String EXTRA_REPORT_CONTROLS = "ReportHtmlViewerActivity.EXTRA_REPORT_CONTROLS";
    // Result Code
    public static final int REQUEST_REPORT_PARAMETERS = 100;
    @Extra
    ResourceLookup resource;
    @Bean
    ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    ServerInfoManager infoManager;

    @Inject
    JsRestClient jsRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            scrollableTitleHelper.injectTitle(this, resource.getLabel());
        }

        if (savedInstanceState == null) {
            infoManager.getServerInfo(getSpiceManager(), new ServerInfoManager.InfoCallback() {
                @Override
                public void onInfoReceived(ServerInfoSnapshot serverInfo) {
                    commitFragments(serverInfo);
                }
            });
        }
    }

    private void commitFragments(ServerInfoSnapshot serverInfo) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        PaginationManagerFragment paginationManagerFragment = PaginationManagerFragment_
                .builder().versionCode(serverInfo.getVersionCode()).build();
        transaction.add(R.id.control, paginationManagerFragment, PaginationManagerFragment.TAG);

        ReportExecutionFragment reportExecutionFragment = ReportExecutionFragment_.builder()
                .resource(resource).versionCode(serverInfo.getVersionCode()).build();
        transaction.add(reportExecutionFragment, ReportExecutionFragment.TAG);

        ReportActionFragment reportActionFragment = ReportActionFragment_.builder()
                .resource(resource).build();
        transaction.add(reportActionFragment, ReportActionFragment.TAG);

        FilterManagerFragment filterManagerFragment = FilterManagerFragment_.builder()
                .resource(resource).build();
        transaction.add(filterManagerFragment, FilterManagerFragment.TAG);

        transaction.commit();
    }

    @OptionsItem(android.R.id.home)
    final void goBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSpiceManager().removeDataFromCache(InputControlsList.class);
        getSpiceManager().removeDataFromCache(ReportExecutionResponse.class);
        getSpiceManager().removeDataFromCache(ExportExecution.class);
        getSpiceManager().removeDataFromCache(ReportDataResponse.class);
    }
}