package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.repository.ReportRepository;
import com.jaspersoft.android.jaspermobile.domain.service.ObservableExecutionService;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReloadReportCase extends AbstractSimpleUseCase<Void> {
    private final ReportRepository mReportRepository;

    public ReloadReportCase(ReportRepository reportRepository) {
        mReportRepository = reportRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return mReportRepository.reset()
                .flatMap(new Func1<Void, Observable<ObservableExecutionService>>() {
                    @Override
                    public Observable<ObservableExecutionService> call(Void aVoid) {
                        return mReportRepository.runReport();
                    }
                })
                .flatMap(new Func1<ObservableExecutionService, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(ObservableExecutionService executionService) {
                        return Observable.just(null);
                    }
                });
    }
}
