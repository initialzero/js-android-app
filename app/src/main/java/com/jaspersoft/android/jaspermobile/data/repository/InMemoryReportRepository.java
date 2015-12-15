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

package com.jaspersoft.android.jaspermobile.data.repository;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;
import com.jaspersoft.android.jaspermobile.domain.service.ReportExecutionService;
import com.jaspersoft.android.jaspermobile.domain.service.ReportService;
import com.jaspersoft.android.jaspermobile.data.mapper.ReportParamsTransformer;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;
import com.octo.android.robospice.persistence.memory.LruCache;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class InMemoryReportRepository implements ReportRepository {

    private final String mReportUri;
    private final ReportParamsStorage mReportParamsStorage;
    private final ReportParamsTransformer mReportParamsTransformer;

    private final ReportService mReportService;

    private final LruCache<String, ReportPage> mPagesCache = new LruCache<>(10);
    private ReportExecutionService mExecutionCache;
    private Integer mPagesCountCache;
    private Boolean mIsMultiPage;

    public InMemoryReportRepository(
            String reportUri,
            ReportService reportService,
            ReportParamsStorage reportParamsStorage, ReportParamsTransformer reportParamsTransformer) {
        mReportService = reportService;
        mReportParamsStorage = reportParamsStorage;
        mReportUri = reportUri;
        mReportParamsTransformer = reportParamsTransformer;
    }

    @Override
    public Observable<ReportPage> getPage(String range) {
        List<ReportParameter> repoParams = getParameters();
        Observable<ReportPage> memory = createPagesMemorySource(range);
        Observable<ReportPage> network = createPagesNetworkSource(range, repoParams);
        return rx.Observable.concat(memory, network).first();
    }

    @Override
    public Observable<ReportExecutionService> runReport() {
        List<ReportParameter> params = getParameters();
        Observable<ReportExecutionService> network = createNetworkExecutionSource(params);
        Observable<ReportExecutionService> memory = createMemoryExecutionSource();
        return Observable.concat(memory, network).first();
    }

    @Override
    public Observable<Void> updateReport() {
        return mExecutionCache.update(getParameters()).doOnNext(new Action1<ReportExecutionService>() {
            @Override
            public void call(ReportExecutionService executionService) {
                mExecutionCache = executionService;
                mPagesCountCache = null;
                mIsMultiPage = null;
                mPagesCache.evictAll();
            }
        }).flatMap(new Func1<ReportExecutionService, Observable<Void>>() {
            @Override
            public Observable<Void> call(ReportExecutionService executionService) {
                return Observable.just(null);
            }
        });
    }

    @NonNull
    private List<ReportParameter> getParameters() {
        List<com.jaspersoft.android.sdk.client.oxm.report.ReportParameter> params =
                mReportParamsStorage.getInputControlHolder(mReportUri).getReportParams();
        return mReportParamsTransformer.transform(params);
    }

    @Override
    public Observable<List<InputControl>> getControls() {
        return mReportService.loadControls(mReportUri).doOnNext(new Action1<List<InputControl>>() {
            @Override
            public void call(List<InputControl> inputControls) {
                mReportParamsStorage.getInputControlHolder(mReportUri).setInputControls(inputControls);
            }
        });
    }

    @Override
    public Observable<Integer> getTotalPages() {
        Observable<Integer> memory = createTotalPagesMemorySource();
        Observable<Integer> network = createTotalPagesNetworkSource();
        return Observable.concat(memory, network).first();
    }

    @Override
    public Observable<Boolean> isMultiPage() {
        Observable<Boolean> memory = createIsMultiPageMemorySource();
        Observable<Boolean> network = createIsMultiPageNetworkSource();
        return Observable.concat(memory, network).first();
    }

    private Observable<Boolean> createIsMultiPageMemorySource() {
        if (mIsMultiPage == null) {
            return Observable.empty();
        }
        return Observable.just(mIsMultiPage);
    }

    private Observable<Boolean> createIsMultiPageNetworkSource() {
        return mExecutionCache.downloadExport("2")
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends ReportPage>>() {
                    @Override
                    public Observable<? extends ReportPage> call(Throwable throwable) {
                        return Observable.just(null);
                    }
                }).doOnNext(new Action1<ReportPage>() {
                    @Override
                    public void call(ReportPage page) {
                        if (page != null) {
                            mPagesCache.put("2", page);
                        }
                    }
                })
                .flatMap(new Func1<ReportPage, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(ReportPage page) {
                        return Observable.just(page != null);
                    }
                }).doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean multiPageFlag) {
                        mIsMultiPage = multiPageFlag;
                    }
                });
    }

    private Observable<Integer> createTotalPagesMemorySource() {
        if (mPagesCountCache == null) {
            return Observable.empty();
        }
        return Observable.just(mPagesCountCache);
    }

    private Observable<Integer> createTotalPagesNetworkSource() {
        return runReport().flatMap(new Func1<ReportExecutionService, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(ReportExecutionService reportExecutionService) {
                return reportExecutionService.loadTotalPages();
            }
        }).doOnNext(new Action1<Integer>() {
            @Override
            public void call(Integer pagesCount) {
                mPagesCountCache = pagesCount;
            }
        });
    }

    private Observable<ReportPage> createPagesNetworkSource(final String range, final List<ReportParameter> params) {
        return runReport()
                .switchMap(new Func1<ReportExecutionService, Observable<ReportPage>>() {
                    @Override
                    public Observable<ReportPage> call(ReportExecutionService reportExecutionService) {
                        return reportExecutionService.downloadExport(range);
                    }
                }).doOnNext(new Action1<ReportPage>() {
                    @Override
                    public void call(ReportPage page) {
                        mPagesCache.put(range, page);
                    }
                });
    }

    private Observable<ReportPage> createPagesMemorySource(final String pages) {
        return Observable.defer(new Func0<Observable<ReportPage>>() {
            @Override
            public Observable<ReportPage> call() {
                ReportPage page = mPagesCache.get(pages);
                if (page == null) {
                    return Observable.empty();
                } else {
                    if (page.isFinal()) {
                        return Observable.just(page);
                    }
                    return Observable.empty();
                }
            }
        });
    }


    private Observable<ReportExecutionService> createMemoryExecutionSource() {
        if (mExecutionCache == null) {
            return Observable.empty();
        }
        return Observable.just(mExecutionCache);
    }

    private Observable<ReportExecutionService> createNetworkExecutionSource(List<ReportParameter> params) {
        return mReportService.runReport(mReportUri, params)
                .doOnNext(new Action1<ReportExecutionService>() {
                    @Override
                    public void call(ReportExecutionService reportExecutionService) {
                        mExecutionCache = reportExecutionService;
                    }
                });
    }
}
