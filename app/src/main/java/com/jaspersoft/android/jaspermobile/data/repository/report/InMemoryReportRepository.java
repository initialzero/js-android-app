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

package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerReport;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.report.ReportExecutionOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerReport
public final class InMemoryReportRepository implements ReportRepository {
    private final RxReportService mRxReportService;
    private final ReportCache mReportCache;
    private final ReportPageCache mReportPageCache;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;

    private Observable<Report> mReloadReportCommand;
    private Observable<Report> mUpdateReportCommand;
    private Observable<Report> mGetReportCommand;

    @Inject
    public InMemoryReportRepository(RxReportService rxReportService,
                                    ReportCache reportCache,
                                    ReportPageCache reportPageCache, ReportParamsCache reportParamsCache,
                                    ReportParamsMapper reportParamsMapper) {
        mRxReportService = rxReportService;
        mReportCache = reportCache;
        mReportPageCache = reportPageCache;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
    }

    @NonNull
    @Override
    public Observable<Report> getReport(@NonNull final String uri) {
        if (mGetReportCommand == null) {
            Observable<Report> memorySource = Observable.defer(new Func0<Observable<Report>>() {
                @Override
                public Observable<Report> call() {
                    Report report = mReportCache.get(uri);
                    if (report == null) {
                        return Observable.empty();
                    }
                    return Observable.just(report);
                }
            });

            Observable<Report> networkSource = Observable.defer(new Func0<Observable<Report>>() {
                @Override
                public Observable<Report> call() {
                    List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> legacyParams = mReportParamsCache.get(uri);
                    List<ReportParameter> params = mReportParamsMapper.toRetrofittedParams(legacyParams);

                    ReportExecutionOptions options = ReportExecutionOptions.builder()
                            .withFormat(ReportFormat.HTML)
                            .withFreshData(false)
                            .withParams(params)
                            .build();

                    return mRxReportService.run(uri, options)
                            .map(new Func1<RxReportExecution, Report>() {
                                @Override
                                public Report call(RxReportExecution execution) {
                                    return new Report(execution, uri);
                                }
                            })
                            .doOnNext(new Action1<Report>() {
                                @Override
                                public void call(Report report) {
                                    mReportCache.put(uri, report);
                                }
                            });
                }
            });

            mGetReportCommand = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mGetReportCommand = null;
                        }
                    });
        }
        return mGetReportCommand;
    }

    @NonNull
    @Override
    public Observable<Report> reloadReport(@NonNull final String uri) {
        if (mReloadReportCommand == null) {
            mReloadReportCommand = Observable.defer(new Func0<Observable<Report>>() {
                @Override
                public Observable<Report> call() {
                    mReportPageCache.removePages(uri);
                    mReportCache.remove(uri);
                    return getReport(uri);
                }
            });
            mReloadReportCommand = mReloadReportCommand.cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mReloadReportCommand = null;
                        }
                    });
        }
        return mReloadReportCommand;
    }

    @NonNull
    @Override
    public Observable<Report> updateReport(@NonNull final String uri) {
        if (mUpdateReportCommand == null) {
            mUpdateReportCommand = Observable.defer(new Func0<Observable<Report>>() {
                @Override
                public Observable<Report> call() {
                    mReportPageCache.removePages(uri);
                    mReportCache.remove(uri);

                    return getReport(uri).flatMap(new Func1<Report, Observable<RxReportExecution>>() {
                        @Override
                        public Observable<RxReportExecution> call(Report report) {
                            List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> legacyParams = mReportParamsCache.get(uri);
                            List<ReportParameter> params = mReportParamsMapper.toRetrofittedParams(legacyParams);

                            return report.getExecution().updateExecution(params);
                        }
                    }).map(new Func1<RxReportExecution, Report>() {
                        @Override
                        public Report call(RxReportExecution execution) {
                            return new Report(execution, uri);
                        }
                    }).doOnNext(new Action1<Report>() {
                        @Override
                        public void call(Report report) {
                            mReportCache.put(uri, report);
                        }
                    });
                }
            });

            mUpdateReportCommand = mUpdateReportCommand.cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mUpdateReportCommand = null;
                        }
                    });
        }
        return mUpdateReportCommand;
    }
}
