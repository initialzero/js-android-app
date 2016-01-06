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

package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerReport;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerReport
public class GetReportTotalPagesPropertyCase extends AbstractSimpleUseCase<Integer> {
    private final ReportRepository mReportRepository;
    private final ReportPropertyRepository mReportPropertyRepository;
    private final String mReportUri;

    @Inject
    public GetReportTotalPagesPropertyCase(PreExecutionThread preExecutionThread,
                                           PostExecutionThread postExecutionThread,
                                           ReportRepository reportRepository,
                                           ReportPropertyRepository reportPropertyRepository,
                                           @Named("report_uri") String reportUri
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
        mReportPropertyRepository = reportPropertyRepository;
        mReportUri = reportUri;
    }

    @Override
    protected Observable<Integer> buildUseCaseObservable() {
        return mReportRepository.getReport(mReportUri)
                .flatMap(new Func1<Report, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Report report) {
                        return mReportPropertyRepository.getTotalPagesProperty(report);
                    }
                });
    }
}
