package com.jaspersoft.android.jaspermobile.domain.interactor.dashboard;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardControlComponent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetDashboardVisualizeParamsCase extends AbstractUseCase<String, String> {

    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;
    private final ControlsRepository mControlsRepository;

    @Inject
    public GetDashboardVisualizeParamsCase(PreExecutionThread preExecutionThread,
                                           PostExecutionThread postExecutionThread,

                                           ReportParamsCache reportParamsCache, ReportParamsMapper reportParamsMapper, ControlsRepository controlsRepository) {
        super(preExecutionThread, postExecutionThread);
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
        mControlsRepository = controlsRepository;
    }

    @Override
    protected Observable<String> buildUseCaseObservable(@NotNull final String dashboardUri) {
        return mControlsRepository.listDashboardControlComponents(dashboardUri)
                .flatMap(new Func1<List<DashboardControlComponent>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<DashboardControlComponent> components) {
                        List<ReportParameter> reportParameters = mReportParamsCache.get(dashboardUri);
                        reportParameters = mReportParamsMapper.adaptDashboardControlComponents(reportParameters, components);
                        String jsonParams = mReportParamsMapper.legacyParamsToJson(reportParameters);
                        return Observable.just(jsonParams);
                    }
                });
    }
}
