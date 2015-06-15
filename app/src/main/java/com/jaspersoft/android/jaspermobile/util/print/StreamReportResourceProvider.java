/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetExportOutputRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.springframework.http.client.ClientHttpResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class StreamReportResourceProvider implements ResourceProvider<ClientHttpResponse> {
    private final JsRestClient mJsRestClient;
    private final ResourceLookup mResource;
    private final List<ReportParameter> mReportParameters;
    private String exportOutput;
    private String executionId;

    private StreamReportResourceProvider(Builder builder) {
        mJsRestClient = builder.jsRestClient;
        mResource = builder.resource;
        mReportParameters = builder.reportParameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ClientHttpResponse provideResource() {
        try {
            return requestFileExportAsPDF();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ClientHttpResponse requestFileExportAsPDF() throws Exception {
        ReportExecutionRequest request = prepareReportExecutionRequest();

        if (TextUtils.isEmpty(exportOutput) || TextUtils.isEmpty(executionId) ) {
            ReportExecutionResponse exportResponse = mJsRestClient.runReportExecution(request);
            ExportExecution execution = exportResponse.getExports().get(0);
            exportOutput = execution.getId();
            executionId = exportResponse.getRequestId();
        }

        GetExportOutputRequest getExportOutputRequest = new GetExportOutputRequest(mJsRestClient, executionId, exportOutput);
        return getExportOutputRequest.loadDataFromNetwork();
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

    public static class Builder {
        private JsRestClient jsRestClient;
        private ResourceLookup resource;
        private List<ReportParameter> reportParameters;

        public Builder() {
            this.reportParameters = new ArrayList<ReportParameter>();
        }

        public Builder setJsRestClient(@Nullable JsRestClient jsRestClient) {
            this.jsRestClient = jsRestClient;
            return this;
        }

        public Builder setResource(@Nullable ResourceLookup resource) {
            this.resource = resource;
            return this;
        }

        public Builder addReportParameters(@Nullable Collection<ReportParameter> reportParameters) {
            if (reportParameters == null) {
                throw new IllegalArgumentException("Report parameters should not be null");
            }
            this.reportParameters.addAll(reportParameters);
            return this;
        }

        public ResourceProvider<ClientHttpResponse> build() {
            validateDependencies();
            return new StreamReportResourceProvider(this);
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
