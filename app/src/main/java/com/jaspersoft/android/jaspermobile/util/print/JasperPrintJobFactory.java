/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.print;

import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.server.ServerInfo;
import com.jaspersoft.android.jaspermobile.util.server.ServerInfoProvider;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.List;

/**
 * Factory responsible for creation of specific print job for corresponding job
 *
 * @author Tom Koptel
 * @since 2.1
 */
public class JasperPrintJobFactory {

    /**
     * Creates {@link ReportPrintJob} which is responsible for creating custom adapter for native print framework
     * 
     * @param resource SDK object which wraps resource metadata
     * @param context context which is usually comes from activity
     * @param jsRestClient SDK rest client
     * @param reportParameters report parameters user chooses before printing report
     * @return common abstraction around resource printing
     */
    public static ResourcePrintJob createReportPrintJob(Context context, JsRestClient jsRestClient, ResourceLookup resource, List<ReportParameter> reportParameters) {
        ServerInfoProvider serverInfoProvider = ServerInfo.newInstance(context);

        PrintUnit reportPrintUnit = ReportPrintUnit.builder()
                .setResource(resource)
                .setJsRestClient(jsRestClient)
                .addReportParameters(reportParameters)
                .setServerInfoProvider(serverInfoProvider)
                .build();

        return ReportPrintJob.builder(context)
                .setPrintUnit(reportPrintUnit)
                .setPrintName(resource.getLabel())
                .build()
                .printResource();
    }

    /**
     * Creates {@link DashboardPicturePrintJob} or {@link DashboardWebviewPrintJob} jobs responsible for starting native printing
     *
     * @param webView target {@link WebView} instance we would like to print from
     * @param resource SDK object which wraps resource metadata
     * @return common abstraction around resource printing
     */
    public static ResourcePrintJob createDashboardPrintJob(WebView webView, ResourceLookup resource) {
        ServerInfoProvider serverInfoProvider = ServerInfo.newInstance(webView.getContext());
        ServerRelease serverRelease = ServerRelease.parseVersion(serverInfoProvider.getServerVersion());

        if (serverRelease.code() >= ServerRelease.AMBER.code()) {
            return DashboardPicturePrintJob.builder()
                    .setPrintName(resource.getLabel())
                    .setWebView(webView)
                    .build()
                    .printResource();
        } else {
            return DashboardWebviewPrintJob.builder()
                    .setPrintName(resource.getLabel())
                    .setWebView(webView)
                    .build()
                    .printResource();
        }
    }

}
