package com.jaspersoft.android.jaspermobile.domain.interactor.report.option;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.SaveOptionRequest;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.option.ReportOption;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class SaveReportOptionsCase extends AbstractUseCase<ReportOption, SaveOptionRequest> {
    private final ReportOptionsRepository mReportOptionsRepository;

    @Inject
    public SaveReportOptionsCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportOptionsRepository reportOptionsRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportOptionsRepository = reportOptionsRepository;
    }

    @Override
    protected Observable<ReportOption> buildUseCaseObservable(@NonNull SaveOptionRequest request) {
        return mReportOptionsRepository.createReportOptionWithOverride(
                request.getReportUri(),
                request.getLabel(),
                request.getParams()
        );
    }
}
