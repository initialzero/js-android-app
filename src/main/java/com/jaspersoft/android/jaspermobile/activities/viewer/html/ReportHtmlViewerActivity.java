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

import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
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

    private ArrayList<ReportParameter> reportParameters;

    @Override
    protected void loadDataToWebView() {
        // run new report execution
        RunReportExecutionRequest request = new RunReportExecutionRequest(jsRestClient, resourceUri, "HTML", reportParameters);
        serviceManager.execute(request, new RunReportExecutionListener());
    }

    @Override
    protected void initDataFromExtras() {
        super.initDataFromExtras();
        reportParameters = getIntent().getExtras().getParcelableArrayList(EXTRA_REPORT_PARAMETERS);
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class RunReportExecutionListener implements RequestListener<ReportExecutionResponse> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ReportHtmlViewerActivity.this, false);
        }

        @Override
        public void onRequestSuccess(ReportExecutionResponse response) {
            String executionId = response.getRequestId();
            String exportOutput = response.getExports().get(0).getId();
            URI reportUri = jsRestClient.getExportOuptutResourceURI(executionId, exportOutput);
            loadUrl(reportUri.toString());
        }
    }

}
