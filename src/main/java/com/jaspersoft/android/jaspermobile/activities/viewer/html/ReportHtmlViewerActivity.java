/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.viewer.html;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.report.SaveReportActivity;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.net.URI;
import java.util.ArrayList;

/**
 * Activity that performs report viewing in HTML format.
 *
 * @author Ivan Gadzhega
 * @since 1.4
 */
public class ReportHtmlViewerActivity extends BaseHtmlViewerActivity {

    // Extras
    public static final String EXTRA_REPORT_PARAMETERS = "ReportHtmlViewerActivity.EXTRA_REPORT_PARAMETERS";

    // Action Bar IDs
    private static final int ID_AB_SAVE_AS = 34;

    private Menu optionsMenu;
    private ArrayList<ReportParameter> reportParameters;
    private int serverVersion;

    @Override
    protected void loadDataToWebView() {
        GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
        serviceManager.execute(request, new GetServerInfoListener());
    }

    @Override
    protected void initDataFromExtras() {
        super.initDataFromExtras();
        reportParameters = getIntent().getExtras().getParcelableArrayList(EXTRA_REPORT_PARAMETERS);
    }

    //---------------------------------------------------------------------
    // Options Menu
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;

        MenuItem saveAsItem = menu.add(Menu.NONE, ID_AB_SAVE_AS, Menu.NONE, R.string.rv_ab_save_report);
        saveAsItem.setIcon(R.drawable.ic_action_save);
        saveAsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        saveAsItem.setActionView(R.layout.actionbar_indeterminate_progress);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_AB_SAVE_AS:
                if (!FileUtils.isExternalStorageWritable()) {
                    // storage not available
                    Toast.makeText(ReportHtmlViewerActivity.this, R.string.rv_t_external_storage_not_available, Toast.LENGTH_SHORT).show();
                } else if (serverVersion < ServerInfo.VERSION_CODES.EMERALD_MR1) {
                    // feature not supported
                    Toast.makeText(ReportHtmlViewerActivity.this, R.string.rv_t_report_saving_not_supported, Toast.LENGTH_SHORT).show();
                } else {
                    // save report
                    Intent saveReport = new Intent();
                    saveReport.setClass(this, SaveReportActivity.class);
                    saveReport.putExtra(EXTRA_RESOURCE_URI, resourceUri);
                    saveReport.putExtra(EXTRA_RESOURCE_LABEL, resourceLabel);
                    saveReport.putExtra(EXTRA_REPORT_PARAMETERS, reportParameters);
                    startActivity(saveReport);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu == null) return;

        final MenuItem refreshItem = optionsMenu.findItem(ID_AB_SAVE_AS);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetServerInfoListener implements RequestListener<ServerInfo> {
        @Override
        public void onRequestFailure(SpiceException e) {
            RequestExceptionHandler.handle(e, ReportHtmlViewerActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            String outputFormat = "HTML";
            serverVersion = serverInfo.getVersionCode();
            // run new report execution
            if (serverVersion >= ServerInfo.VERSION_CODES.EMERALD_MR1) {
                // POST
                RunReportExecutionRequest request = new RunReportExecutionRequest(jsRestClient,
                        resourceUri, outputFormat, reportParameters);
                serviceManager.execute(request, new RunReportExecutionListener());
            } else {
                // GET
                String reportUrl =
                        jsRestClient.generateReportUrl(resourceUri, reportParameters, outputFormat);
                loadUrl(reportUrl);
                setRefreshActionButtonState(false);
            }
        }
    }

    private class RunReportExecutionListener implements RequestListener<ReportExecutionResponse> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportHtmlViewerActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ReportExecutionResponse response) {
            String executionId = response.getRequestId();
            String exportOutput = response.getExports().get(0).getId();
            URI reportUri = jsRestClient.getExportOuptutResourceURI(executionId, exportOutput);
            loadUrl(reportUri.toString());

            setRefreshActionButtonState(false);
        }
    }

}