package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class UpdateReportExecutionCase extends AbstractUseCase<Void> {
    private final ReportRepository mReportRepository;

    public UpdateReportExecutionCase(ReportRepository reportRepository) {
        mReportRepository = reportRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return mReportRepository.updateReport();
    }
}
