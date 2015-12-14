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

package com.jaspersoft.android.jaspermobile.data.service;

import com.jaspersoft.android.jaspermobile.domain.service.ReportExecutionService;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.report.ReportMetadata;
import com.jaspersoft.android.sdk.service.data.report.ReportOutput;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.report.ExecutionCriteria;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.ReportExport;
import com.jaspersoft.android.sdk.service.report.RunExportCriteria;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class RestReportExecutionService implements ReportExecutionService {
    private final ReportExecution mExecution;

    public RestReportExecutionService(ReportExecution reportExecution) {
        mExecution = reportExecution;
    }

    @Override
    public Observable<String> downloadExport(final String pageRange) {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    RunExportCriteria criteria = RunExportCriteria.builder()
                            .pages(pageRange)
                            .format(ExecutionCriteria.Format.HTML)
                            .create();
                    ReportExport export = mExecution.export(criteria);
                    ReportOutput reportOutput = export.download();
                    InputStream stream = reportOutput.getStream();
                    String page = IOUtils.toString(reportOutput.getStream());
                    IOUtils.closeQuietly(stream);
                    return Observable.just(page);
                } catch (ServiceException e) {
                    return Observable.error(e);
                } catch (IOException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    @Override
    public Observable<Integer> loadTotalPages() {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                try {
                    ReportMetadata metadata = mExecution.waitForReportCompletion();
                    return Observable.just(metadata.getTotalPages());
                } catch (ServiceException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    @Override
    public Observable<ReportExecutionService> update(final List<ReportParameter> newParameters) {
        return Observable.defer(new Func0<Observable<ReportExecutionService>>() {
            @Override
            public Observable<ReportExecutionService> call() {
                try {
                    ReportExecution result = mExecution.updateExecution(newParameters);
                    ReportExecutionService executionService = new RestReportExecutionService(result);
                    return Observable.just(executionService);
                } catch (ServiceException e) {
                    return Observable.error(e);
                }
            }
        });
    }
}
