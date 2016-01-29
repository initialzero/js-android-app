package com.jaspersoft.android.jaspermobile.domain.interactor.report.option;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportOptionValuesCase extends AbstractUseCase<List<InputControlState>, String> {
    private final ReportOptionsRepository mReportOptionsRepository;

    @Inject
    public GetReportOptionValuesCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportOptionsRepository reportOptionsRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportOptionsRepository = reportOptionsRepository;
    }

    @Override
    protected Observable<List<InputControlState>> buildUseCaseObservable(@NonNull String optionUri) {
        return mReportOptionsRepository.getReportOptionStates(optionUri);
    }
}
