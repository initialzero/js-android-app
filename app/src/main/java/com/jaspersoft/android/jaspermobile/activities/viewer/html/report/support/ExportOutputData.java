package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

import com.google.common.base.Preconditions;
import com.jaspersoft.android.sdk.client.oxm.report.ReportDataResponse;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ExportOutputData {
    private final String executionId;
    private final ReportDataResponse response;

    public static Builder builder() {
        return new Builder();
    }

    private ExportOutputData(String executionId, ReportDataResponse response) {
        this.executionId = executionId;
        this.response = response;
    }

    public String getExecutionId() {
        return executionId;
    }

    public boolean isFinal() {
        return response.isFinal();
    }

    public String getData() {
        return response.getData();
    }

    public static class Builder {
        private String executionId;
        private ReportDataResponse response;

        public Builder setExecutionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder setResponse(ReportDataResponse response) {
            this.response = response;
            return this;
        }

        public ExportOutputData create() {
            Preconditions.checkNotNull(executionId);
            Preconditions.checkNotNull(response);
            return new ExportOutputData(executionId, response);
        }
    }
}
