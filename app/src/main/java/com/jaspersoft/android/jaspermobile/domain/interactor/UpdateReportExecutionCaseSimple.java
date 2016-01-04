package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class UpdateReportExecutionCaseSimple extends AbstractSimpleUseCase<Void> {
    private final ReportRepository mReportRepository;

    public UpdateReportExecutionCaseSimple(PreExecutionThread preExecutionThread,
                                           PostExecutionThread postExecutionThread,
                                           ReportRepository reportRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return mReportRepository.updateReport();
    }
}
