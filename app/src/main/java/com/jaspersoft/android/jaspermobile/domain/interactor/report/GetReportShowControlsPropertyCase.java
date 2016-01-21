package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerReport;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerReport
public class GetReportShowControlsPropertyCase extends AbstractSimpleUseCase<Boolean> {
    private final ControlsRepository mControlsRepository;
    private final String mReportUri;

    @Inject
    public GetReportShowControlsPropertyCase(PreExecutionThread preExecutionThread,
                                             PostExecutionThread postExecutionThread,
                                             ControlsRepository controlsRepository,
                                             @Named("report_uri") String reportUri) {
        super(preExecutionThread, postExecutionThread);
        mControlsRepository = controlsRepository;
        mReportUri = reportUri;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return mControlsRepository.listControls(mReportUri)
                .map(new Func1<List<InputControl>, Boolean>() {
                    @Override
                    public Boolean call(List<InputControl> inputControls) {
                        return !inputControls.isEmpty();
                    }
                });
    }
}
