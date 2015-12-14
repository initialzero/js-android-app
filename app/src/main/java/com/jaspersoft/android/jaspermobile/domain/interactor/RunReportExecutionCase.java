package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;
import com.jaspersoft.android.jaspermobile.domain.service.ReportExecutionService;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class RunReportExecutionCase extends AbstractUseCase<Void> {
    private final ReportRepository mReportRepository;

    public RunReportExecutionCase(ReportRepository reportRepository) {
        mReportRepository = reportRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return mReportRepository.runReport()
                .flatMap(new Func1<ReportExecutionService, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(ReportExecutionService executionService) {
                        return Observable.just(null);
                    }
                });
    }
}
