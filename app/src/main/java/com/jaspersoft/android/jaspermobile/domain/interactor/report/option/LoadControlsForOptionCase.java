package com.jaspersoft.android.jaspermobile.domain.interactor.report.option;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.LoadOptionParamsRequest;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * TODO revise interactor after release 2.3. There should be no mentioning of data layer here
 *
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class LoadControlsForOptionCase extends AbstractUseCase<Void, LoadOptionParamsRequest> {
    private final ReportOptionsRepository mReportOptionsRepository;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;

    @Inject
    public LoadControlsForOptionCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportOptionsRepository reportOptionsRepository,
            ReportParamsCache reportParamsCache,
            ReportParamsMapper reportParamsMapper
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportOptionsRepository = reportOptionsRepository;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable(final LoadOptionParamsRequest request) {
        return mReportOptionsRepository.getReportOptionStates(request.getOptionUri())
                .flatMap(new Func1<List<InputControlState>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(List<InputControlState> states) {
                        List<ReportParameter> parameters = mReportParamsMapper.mapStatesToLegacyParams(states);
                        mReportParamsCache.put(request.getReportUri(), parameters);
                        return Observable.just(null);
                    }
                });
    }
}
