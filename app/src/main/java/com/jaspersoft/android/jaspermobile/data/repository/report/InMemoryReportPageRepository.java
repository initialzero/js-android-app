package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.PageRange;
import com.jaspersoft.android.sdk.service.data.report.ReportExportOutput;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;
import com.jaspersoft.android.sdk.service.report.ReportExportOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExport;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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
public final class InMemoryReportPageRepository implements ReportPageRepository {
    private final ReportPageCache mReportPageCache;

    private LruCache<String, Observable<ReportPage>> mOperationCache = new LruCache<>(10);

    @Inject
    public InMemoryReportPageRepository(ReportPageCache reportPageCache) {
        mReportPageCache = reportPageCache;
    }

    @NonNull
    @Override
    public Observable<ReportPage> get(@NonNull final Report report,
                                      @NonNull final String pagePosition) {
        Observable<ReportPage> reportPageObservable = mOperationCache.get(pagePosition);
        if (reportPageObservable == null) {
            Observable<ReportPage> memorySource = Observable.defer(new Func0<Observable<ReportPage>>() {
                @Override
                public Observable<ReportPage> call() {
                    ReportPage reportPage = mReportPageCache.get(report.getReportUri(), pagePosition);
                    if (reportPage == null) {
                        return Observable.empty();
                    }
                    return Observable.just(reportPage);
                }
            });

            Observable<ReportPage> networkSource = Observable.defer(new Func0<Observable<RxReportExport>>() {
                @Override
                public Observable<RxReportExport> call() {
                    RxReportExecution execution = report.getExecution();
                    ReportExportOptions options = ReportExportOptions.builder()
                            .withFormat(ReportFormat.HTML)
                            .withPageRange(PageRange.parse(pagePosition))
                            .build();
                    return execution.export(options);
                }
            }).flatMap(new Func1<RxReportExport, Observable<ReportExportOutput>>() {
                @Override
                public Observable<ReportExportOutput> call(RxReportExport export) {
                    return export.download();
                }
            }).flatMap(new Func1<ReportExportOutput, Observable<ReportPage>>() {
                @Override
                public Observable<ReportPage> call(ReportExportOutput output) {
                    InputStream stream = null;
                    try {
                        stream = output.getStream();
                        String content = IOUtils.toString(stream);
                        return Observable.just(new ReportPage(content, output.isFinal()));
                    } catch (IOException e) {
                        return Observable.error(e);
                    } finally {
                        if (stream != null) {
                            IOUtils.closeQuietly(stream);
                        }
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
                    mReportPageCache.put(report.getReportUri(), pagePosition, page);
                }
            });

            Observable<ReportPage> getPageCommand = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mOperationCache.remove(pagePosition);
                        }
                    });

            reportPageObservable = getPageCommand;
            mOperationCache.put(pagePosition, getPageCommand);
        }

        return reportPageObservable;
    }
}
