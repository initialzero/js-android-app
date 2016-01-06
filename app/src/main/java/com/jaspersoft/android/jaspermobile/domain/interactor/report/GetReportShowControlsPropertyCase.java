package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerReport;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerReport
public class GetReportShowControlsPropertyCase extends AbstractUseCase<Boolean, String> {
    private final ControlsRepository mControlsRepository;

    @Inject
    public GetReportShowControlsPropertyCase(PreExecutionThread preExecutionThread,
                                             PostExecutionThread postExecutionThread,
                                             ControlsRepository controlsRepository) {
        super(preExecutionThread, postExecutionThread);
        mControlsRepository = controlsRepository;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable(@NonNull String uri) {
        return mControlsRepository.listControls(uri)
                .map(new Func1<List<InputControl>, Boolean>() {
                    @Override
                    public Boolean call(List<InputControl> inputControls) {
                        return !inputControls.isEmpty();
                    }
                });
    }
}
