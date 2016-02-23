package com.jaspersoft.android.jaspermobile.domain.interactor.dashboard;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetDashboardControlsCase extends AbstractUseCase<Boolean, String> {
    private final ControlsRepository mControlsRepository;

    @Inject
    public GetDashboardControlsCase(PreExecutionThread preExecutionThread,
                                             PostExecutionThread postExecutionThread,
                                             ControlsRepository controlsRepository) {
        super(preExecutionThread, postExecutionThread);
        mControlsRepository = controlsRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable(@NonNull final String reportUri) {
        return mControlsRepository.listDashboardControls(reportUri)
                .map(new Func1<List<InputControl>, Boolean>() {
                    @Override
                    public Boolean call(List<InputControl> inputControls) {
                        return !inputControls.isEmpty();
                    }
                });
    }
}
