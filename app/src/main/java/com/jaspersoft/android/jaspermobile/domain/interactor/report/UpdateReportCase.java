package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class UpdateReportCase extends AbstractUseCase<ReportPage, String> {
    private final ReportRepository mReportRepository;
    private final ReportPageRepository mReportPageRepository;

    @Inject
    public UpdateReportCase(PreExecutionThread preExecutionThread,
                            PostExecutionThread postExecutionThread,
                            ReportRepository reportRepository,
                            ReportPageRepository reportPageRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
        mReportPageRepository = reportPageRepository;
    }

    @Override
    protected Observable<ReportPage> buildUseCaseObservable(@NonNull final String reportUri) {
        return mReportRepository.updateReport(reportUri)
                .flatMap(new Func1<RxReportExecution, Observable<ReportPage>>() {
                    @Override
                    public Observable<ReportPage> call(RxReportExecution execution) {
                        PageRequest page = new PageRequest.Builder()
                                .setUri(reportUri)
                                .setRange("1")
                                .asHtml()
                                .build();
                        return mReportPageRepository.get(execution, page);
                    }
                });
    }
}
