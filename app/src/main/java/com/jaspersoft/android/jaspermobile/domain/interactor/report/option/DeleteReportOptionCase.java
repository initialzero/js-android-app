package com.jaspersoft.android.jaspermobile.domain.interactor.report.option;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.DeleteOptionRequest;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class DeleteReportOptionCase extends AbstractUseCase<Void, DeleteOptionRequest> {
    private final ReportOptionsRepository mReportOptionsRepository;

    @Inject
    public DeleteReportOptionCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportOptionsRepository reportOptionsRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportOptionsRepository = reportOptionsRepository;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable(@NonNull DeleteOptionRequest request) {
        return mReportOptionsRepository.deleteReportOption(request.getUri(), request.getOptionId());
    }
}
