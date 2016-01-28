package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
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
public final class ValidateInputControlsCase extends AbstractUseCase<List<InputControlState>, String> {
    private final ControlsRepository mControlsRepository;

    @Inject
    public ValidateInputControlsCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ControlsRepository controlsRepository) {
        super(preExecutionThread, postExecutionThread);
        mControlsRepository = controlsRepository;
    }

    @Override
    protected Observable<List<InputControlState>> buildUseCaseObservable(String reportUri) {
        return mControlsRepository.validateControls(reportUri);
    }
}
