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
import com.jaspersoft.android.sdk.client.async.request.RunReportExecutionRequest;
import com.jaspersoft.android.sdk.client.async.request.SaveExportOutputRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class ReportResourceProvider implements FileResourceProvider {
    private final Context mContext;
    private final JsRestClient mJsRestClient;
    private final ResourceLookup mResource;
    private final List<ReportParameter> mReportParameters;

    private ReportResourceProvider(Builder builder) {
        mContext = builder.context;
        mJsRestClient = builder.jsRestClient;
        mResource = builder.resource;
        mReportParameters = builder.reportParameters;
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    @Override
    public File provideResource() {
        try {
            return requestFileExportAsPDF();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File requestFileExportAsPDF() throws Exception {
        File testPdfFile = prepareTempFile();
        ReportExecutionRequest request = prepareReportExecutionRequest();

        RunReportExecutionRequest runReportExecutionRequest = new RunReportExecutionRequest(mJsRestClient, request);
        ReportExecutionResponse exportResponse = runReportExecutionRequest.loadDataFromNetwork();
        ExportExecution execution = exportResponse.getExports().get(0);
        String exportOutput = execution.getId();
        String executionId = exportResponse.getRequestId();

        SaveExportOutputRequest saveExportOutputRequest = new SaveExportOutputRequest(mJsRestClient, executionId, exportOutput, testPdfFile);
        return saveExportOutputRequest.loadDataFromNetwork();
    }

    private ReportExecutionRequest prepareReportExecutionRequest() {
        ReportExecutionRequest executionRequest = new ReportExecutionRequest();
        executionRequest.setReportUnitUri(mResource.getUri());
        executionRequest.setInteractive(false);
        executionRequest.setOutputFormat("PDF");
        executionRequest.setEscapedAttachmentsPrefix("./");

        if (!mReportParameters.isEmpty()) {
            executionRequest.setParameters(mReportParameters);
        }

        return executionRequest;
    }

    private File prepareTempFile() {
        File externalFile = mContext.getExternalFilesDir(null);
        String tmpResource = mResource.getLabel() + ".pdf";
        return new File(externalFile, tmpResource);
    }

    public static class Builder {
        private JsRestClient jsRestClient;
        private ResourceLookup resource;
        private List<ReportParameter> reportParameters;
        private final Context context;

        public Builder(Context context) {
            this.context = context;
            this.reportParameters = new ArrayList<ReportParameter>();
        }

        public Builder setJsRestClient(JsRestClient jsRestClient) {
            this.jsRestClient = jsRestClient;
            return this;
        }

        public Builder setResource(ResourceLookup resource) {
            this.resource = resource;
            return this;
        }

        public Builder addReportParameters(Collection<ReportParameter> reportParameters) {
            if (reportParameters == null) {
                throw new IllegalArgumentException("Report parameters should not be null");
            }
            this.reportParameters.addAll(reportParameters);
            return this;
        }

        public FileResourceProvider build() {
            validateDependencies();
            return new ReportResourceProvider(this);
        }

        private void validateDependencies() {
            if (jsRestClient == null) {
                throw new IllegalStateException("JsRestClient should not be null");
            }
            if (resource == null) {
                throw new IllegalStateException("Resource should not be null");
            }
        }
    }
}
