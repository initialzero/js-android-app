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

import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class ReportPrinter implements ResourcePrinter {
    private Context context;
    private JsRestClient jsRestClient;
    private ResourceLookup resource;
    private List<ReportParameter> reportParameters;

    private ReportPrinter() {}

    public ReportPrinter withJsRestClient(JsRestClient jsRestClient) {
        this.jsRestClient = jsRestClient;
        return this;
    }

    public ReportPrinter withReportParameters(List<ReportParameter> reportParameters) {
        this.reportParameters = reportParameters;
        return this;
    }

    public ReportPrinter withResource(ResourceLookup resource) {
        this.resource = resource;
        return this;
    }

    public ReportPrinter withContext(Context context) {
        this.context = context;
        return this;
    }

    public static ReportPrinter get() {
        return new ReportPrinter();
    }

    @Override
    public ResourcePrintJob print() {
        PrintUnit reportPrintUnit = ReportPrintUnit.builder()
                .setResource(resource)
                .setJsRestClient(jsRestClient)
                .addReportParameters(reportParameters)
                .build();

        return ReportPrintJob.builder(context)
                .setPrintUnit(reportPrintUnit)
                .setPrintName(resource.getLabel())
                .build()
                .printResource();
    }
}
