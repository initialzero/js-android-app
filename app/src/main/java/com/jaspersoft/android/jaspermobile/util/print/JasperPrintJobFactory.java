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

package com.jaspersoft.android.jaspermobile.util.print;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.server.InfoProvider;
import com.jaspersoft.android.jaspermobile.util.server.ServerInfoProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import java.util.List;

/**
 * Factory responsible for creation of specific print job for corresponding job
 *
 * @author Tom Koptel
 * @since 2.1
 */
public final class JasperPrintJobFactory {

    /**
     * Creates {@link ResourcePrintJob} for report which is responsible for creating custom adapter for native print framework
     * <br/>
     * Throws {@link IllegalArgumentException} if {@link ResourceLookup} type is not reportUnit
     *
     * @param resource         SDK object which wraps resource metadata
     * @param context          context which represents current {@link Activity}
     * @param jsRestClient     SDK rest client
     * @param reportParameters report parameters user chooses before printing report
     * @return common abstraction around resource printing
     */
    public static ResourcePrintJob createReportPrintJob(Context context, JsRestClient jsRestClient, ResourceLookup resource, List<ReportParameter> reportParameters) {
        if (resource.getResourceType() != ResourceLookup.ResourceType.reportUnit) {
            throw new IllegalArgumentException("Incorrect resource type. It should be 'reportUnit' but was: " + String.valueOf(resource.getResourceType()));
        }

        ServerInfoProvider serverInfoProvider = new InfoProvider(context);
        PrintUnit reportPrintUnit = new ReportPrintUnit(jsRestClient, resource, reportParameters, serverInfoProvider);
        return new ReportPrintJob(context, reportPrintUnit, resource.getLabel());
    }

    /**
     * Creates {@link ResourcePrintJob} for report which is responsible for creating custom adapter for native print dashboard
     * <br/>
     * Throws {@link IllegalArgumentException} if {@link ResourceLookup} type is not dashboard
     *
     * @param webView  target {@link WebView} instance we would like to print from
     * @param resource SDK object which wraps resource metadata
     * @return common abstraction around resource printing
     */
    public static ResourcePrintJob createDashboardPrintJob(WebView webView, ResourceLookup resource) {
        if (resource.getResourceType() != ResourceLookup.ResourceType.dashboard && resource.getResourceType() != ResourceLookup.ResourceType.legacyDashboard) {
            throw new IllegalArgumentException("Incorrect resource type. It should be 'dashboard' or 'legacyDashboard' but was: " + String.valueOf(resource.getResourceType()));
        }

        ServerInfoProvider serverInfoProvider = new InfoProvider(webView.getContext());
        ServerVersion version = serverInfoProvider.getVersion();

        String printName = resource.getLabel();
        if (resource.getResourceType() != ResourceLookup.ResourceType.legacyDashboard || version.greaterThanOrEquals(ServerVersion.v6)) {
            return new DashboardPicturePrintJob(webView, printName);
        } else {
            return new DashboardWebViewPrintJob(webView, printName);
        }
    }

}
