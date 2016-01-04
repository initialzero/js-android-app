package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;
import com.jaspersoft.android.jaspermobile.domain.service.ObservableExecutionService;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class RunReportExecutionCase extends AbstractSimpleUseCase<Void> {
    private final ReportRepository mReportRepository;

    public RunReportExecutionCase(PreExecutionThread preExecutionThread,
                                  PostExecutionThread postExecutionThread,
                                  ReportRepository reportRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return mReportRepository.runReport()
                .flatMap(new Func1<ObservableExecutionService, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(ObservableExecutionService executionService) {
                        return Observable.just(null);
                    }
                });
    }
}
