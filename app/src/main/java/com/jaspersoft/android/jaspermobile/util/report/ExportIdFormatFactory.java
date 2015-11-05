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

package com.jaspersoft.android.jaspermobile.util.report;

import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class ExportIdFormatFactory {
    private final ExportExecution exportExecution;
    private final ExportsRequest exportsRequest;

    // TODO replace with factory method
    private ExportIdFormatFactory(Builder builder) {
        this.exportExecution = builder.exportExecution;
        this.exportsRequest = builder.exportsRequest;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ExportIdFormat createAdapter(String serverVersion) {
        ServerRelease release = ServerRelease.parseVersion(serverVersion);
        switch (release) {
            case EMERALD_MR2:
                return new AmberExportIdFormat(exportsRequest);
            default:
                return new DefaultExportIdFormat(exportExecution);
        }
    }

    public static class Builder {
        private ExportExecution exportExecution;
        private ExportsRequest exportsRequest;

        public Builder setExportExecution(ExportExecution exportExecution) {
            this.exportExecution = exportExecution;
            return this;
        }

        public Builder setExportsRequest(ExportsRequest exportsRequest) {
            this.exportsRequest = exportsRequest;
            return this;
        }

        public ExportIdFormatFactory build() {
            return new ExportIdFormatFactory(this);
        }
    }

}
