/*
 * Copyright � 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.repository.report.page;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.sdk.service.data.report.PageRange;
import com.jaspersoft.android.sdk.service.data.report.ReportExportOutput;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.ReportExport;
import com.jaspersoft.android.sdk.service.report.ReportExportOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * @author Tom Koptel
 * @since 2.5
 */
class RawPageCreator extends PageCreator {
    private final PageRequest mPageRequest;
    private final ReportExecution mExecution;

    RawPageCreator(PageRequest pageRequest, ReportExecution execution) {
        mPageRequest = pageRequest;
        mExecution = execution;
    }

    @NonNull
    @Override
    public ReportPage create() throws Exception {
        String range = mPageRequest.getRange();
        PageRange pageRange = PageRange.parse(range);

        ReportExportOptions options = ReportExportOptions.builder()
                .withFormat(ReportFormat.valueOf(mPageRequest.getFormat()))
                .withPageRange(pageRange)
                .build();

        ReportExport export = mExecution.export(options);
        ReportExportOutput output = export.download();

        InputStream reportExport = output.getStream();

        try {
            byte[] content = IOUtils.toByteArray(reportExport);
            return new ReportPage(content, output.isFinal());
        } finally {
            IOUtils.closeQuietly(reportExport);
        }
    }
}
