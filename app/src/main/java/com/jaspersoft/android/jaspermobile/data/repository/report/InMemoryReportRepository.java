/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPropertyCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.jaspersoft.android.sdk.service.internal.ReportExceptionMapper;
import com.jaspersoft.android.sdk.service.report.ReportExecutionOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportRepository implements ReportRepository {

    private final ReportPageCache mReportPageCache;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;
    private final ReportCache mReportCache;
    private final ReportPropertyCache mReportPropertyCache;
    private final JasperRestClient mJasperRestClient;

    @Inject
    public InMemoryReportRepository(JasperRestClient jasperRestClient,
                                    ReportPageCache reportPageCache,
                                    ReportParamsCache reportParamsCache,
                                    ReportParamsMapper reportParamsMapper,
                                    ReportCache reportCache,
                                    ReportPropertyCache reportPropertyCache
    ) {
        mJasperRestClient = jasperRestClient;
        mReportPageCache = reportPageCache;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
        mReportCache = reportCache;
        mReportPropertyCache = reportPropertyCache;
    }

    @NonNull
    @Override
    public Observable<RxReportExecution> getReport(@NonNull final String uri) {
        Observable<RxReportExecution> memorySource = Observable.defer(new Func0<Observable<RxReportExecution>>() {
            @Override
            public Observable<RxReportExecution> call() {
                RxReportExecution execution = mReportCache.get(uri);
                if (execution == null) {
                    return Observable.empty();
                }
                return Observable.just(execution);
            }
        });

        Observable<RxReportExecution> networkSource = Observable.defer(new Func0<Observable<RxReportExecution>>() {
            @Override
            public Observable<RxReportExecution> call() {
                List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> legacyParams = mReportParamsCache.get(uri);
                List<ReportParameter> params = mReportParamsMapper.legacyParamsToRetrofitted(legacyParams);

                final ReportExecutionOptions options = ReportExecutionOptions.builder()
                        .withFormat(ReportFormat.HTML)
                        .withFreshData(false)
                        .withParams(params)
                        .build();

                return mJasperRestClient.reportService().flatMap(new Func1<RxReportService, Observable<RxReportExecution>>() {
                    @Override
                    public Observable<RxReportExecution> call(RxReportService reportService) {
                        return reportService.run(uri, options);
                    }
                });
            }
        }).doOnNext(new Action1<RxReportExecution>() {
            @Override
            public void call(RxReportExecution execution) {
                mReportCache.put(uri, execution);
            }
        });

        return Observable.concat(memorySource, networkSource)
                .first()
                .cache();
    }

    @NonNull
    @Override
    public Observable<RxReportExecution> reloadReport(@NonNull final String uri) {
        return Observable.defer(new Func0<Observable<RxReportExecution>>() {
            @Override
            public Observable<RxReportExecution> call() {
                evictReportCache(uri);
                return getReport(uri);
            }
        });
    }

    @NonNull
    @Override
    public Observable<RxReportExecution> updateReport(@NonNull final String uri) {
        return Observable.defer(new Func0<Observable<RxReportExecution>>() {
                @Override
                public Observable<RxReportExecution> call() {
                    evictReportCache(uri);

                    return getReport(uri).flatMap(new Func1<RxReportExecution, Observable<RxReportExecution>>() {
                        @Override
                        public Observable<RxReportExecution> call(RxReportExecution oldExecution) {
                            List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> legacyParams = mReportParamsCache.get(uri);
                            List<ReportParameter> params = mReportParamsMapper.legacyParamsToRetrofitted(legacyParams);

                            return oldExecution.updateExecution(params);
                        }
                    }).doOnNext(new Action1<RxReportExecution>() {
                        @Override
                        public void call(RxReportExecution newExecution) {
                            mReportCache.put(uri, newExecution);
                        }
                    });
                }
            });
    }

    @Override
    public void flushReport(String reportUri) {
        evictReportCache(reportUri);
        evictParamsCache(reportUri);
    }

    private void evictReportCache(@NonNull String uri) {
        mReportCache.evict(uri);
        mReportPageCache.evictAll();
        mReportPropertyCache.evict(uri);
    }

    private void evictParamsCache(String reportUri) {
        mReportParamsCache.evict(reportUri);
    }
}
