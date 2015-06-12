/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

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
            if (executionId == null) {
                throw new IllegalStateException("Execution id is null");
            }
            if (response == null) {
                throw new IllegalStateException("Response id is null");
            }
            return new ExportOutputData(executionId, response);
        }
    }
}
