package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
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
public class UpdateReportCase extends AbstractSimpleUseCase<ReportPage> {
    private final ReportRepository mReportRepository;
    private final ReportPageRepository mReportPageRepository;
    private final String mReportUri;

    @Inject
    public UpdateReportCase(PreExecutionThread preExecutionThread,
                            PostExecutionThread postExecutionThread,
                            ReportRepository reportRepository,
                            ReportPageRepository reportPageRepository,
                            @Named("report_uri") String reportUri
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
        mReportPageRepository = reportPageRepository;
        mReportUri = reportUri;
    }

    @Override
    protected Observable<ReportPage> buildUseCaseObservable() {
        return mReportRepository.updateReport(mReportUri)
                .flatMap(new Func1<Report, Observable<ReportPage>>() {
                    @Override
                    public Observable<ReportPage> call(Report report) {
                        return mReportPageRepository.get(report, "1");
                    }
                });
    }
}
