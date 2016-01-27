package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.ReportMetadata;

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
@PerProfile
public final class InMemoryReportPropertyRepository implements ReportPropertyRepository {
    private final ReportPageRepository mReportPageRepository;
    private Observable<Boolean> mGetMultiPageCommand;
    private Observable<Integer> mGetTotalPagesCommand;

    @Inject
    public InMemoryReportPropertyRepository(ReportPageRepository reportPageRepository) {
        mReportPageRepository = reportPageRepository;
    }

    @NonNull
    @Override
    public Observable<Boolean> getMultiPageProperty(@NonNull final Report report) {
        if (mGetMultiPageCommand == null) {
            Observable<Boolean> memorySource = Observable.defer(new Func0<Observable<Boolean>>() {
                @Override
                public Observable<Boolean> call() {
                    Boolean multiPage = report.getMultiPage();
                    if (multiPage == null) {
                        return Observable.empty();
                    }
                    return Observable.just(multiPage);
                }
            });
            Observable<Boolean> networkSource = Observable.defer(new Func0<Observable<Boolean>>() {
                @Override
                public Observable<Boolean> call() {
                    return mReportPageRepository.get(report, "2")
                            .map(new Func1<ReportPage, Boolean>() {
                                @Override
                                public Boolean call(ReportPage page) {
                                    return !ReportPage.EMPTY.equals(page);
                                }
                            });
                }
            }).doOnNext(new Action1<Boolean>() {
                @Override
                public void call(Boolean multiPage) {
                    report.setMultiPage(multiPage);
                }
            });
            mGetMultiPageCommand = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mGetMultiPageCommand = null;
                        }
                    });
        }
        return mGetMultiPageCommand;
    }

    @NonNull
    @Override
    public Observable<Integer> getTotalPagesProperty(@NonNull final Report report) {
        if (mGetTotalPagesCommand == null) {
            Observable<Integer> memorySource = Observable.defer(new Func0<Observable<Integer>>() {
                @Override
                public Observable<Integer> call() {
                    Integer totalPages = report.getTotalPages();
                    if (totalPages == null) {
                        return Observable.empty();
                    }
                    return Observable.just(totalPages);
                }
            });
            Observable<Integer> networkSource = Observable.defer(new Func0<Observable<Integer>>() {
                @Override
                public Observable<Integer> call() {
                    return report.getExecution()
                            .waitForReportCompletion()
                            .map(new Func1<ReportMetadata, Integer>() {
                                @Override
                                public Integer call(ReportMetadata reportMetadata) {
                                    return reportMetadata.getTotalPages();
                                }
                            });
                }
            }).doOnNext(new Action1<Integer>() {
                @Override
                public void call(Integer pages) {
                    report.setTotalPages(pages);
                }
            });

            mGetTotalPagesCommand = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mGetTotalPagesCommand = null;
                        }
                    });
        }
        return mGetTotalPagesCommand;
    }
}
