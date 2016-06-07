/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
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

package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.repository.report.page.PageCreator;
import com.jaspersoft.android.jaspermobile.data.repository.report.page.PageCreatorFactory;
import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

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
public final class InMemoryReportPageRepository implements ReportPageRepository {
    private final ReportPageCache mReportPageCache;
    private final PageCreatorFactory mPageCreatorFactory;

    @Inject
    public InMemoryReportPageRepository(
            PageCreatorFactory pageCreatorFactory,
            ReportPageCache reportPageCache
    ) {
        mPageCreatorFactory = pageCreatorFactory;
        mReportPageCache = reportPageCache;
    }

    @NonNull
    @Override
    public Observable<ReportPage> get(@NonNull final RxReportExecution execution, @NonNull final PageRequest pageRequest) {
        Observable<ReportPage> memorySource = Observable.defer(new Func0<Observable<ReportPage>>() {
            @Override
            public Observable<ReportPage> call() {
                ReportPage reportPage = mReportPageCache.get(pageRequest);
                if (reportPage == null) {
                    return Observable.empty();
                }
                return Observable.just(reportPage);
            }
        });

        Observable<ReportPage> networkSource = Observable.defer(new Func0<Observable<ReportPage>>() {
            @Override
            public Observable<ReportPage> call() {
                ReportExecution reportExecution = execution.toBlocking();
                PageCreator pageCreator = mPageCreatorFactory.create(pageRequest, reportExecution);

                try {
                    return Observable.just(pageCreator.create());
                } catch (Exception ex) {
                    return Observable.error(ex);
                }
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ReportPage>>() {
            @Override
            public Observable<? extends ReportPage> call(Throwable throwable) {
                if (throwable instanceof ServiceException) {
                    ServiceException serviceException = (ServiceException) throwable;
                    if (serviceException.code() == StatusCodes.EXPORT_EXECUTION_FAILED) {
                        return Observable.just(ReportPage.EMPTY);
                    }
                }
                return Observable.error(throwable);
            }
        }).doOnNext(new Action1<ReportPage>() {
            @Override
            public void call(ReportPage page) {
                mReportPageCache.put(pageRequest, page);
            }
        });

        return Observable.concat(memorySource, networkSource)
                .first()
                .cache();
    }
}
