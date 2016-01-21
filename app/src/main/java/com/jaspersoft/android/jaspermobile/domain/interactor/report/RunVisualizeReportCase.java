package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.model.visualize.VisualizeComponent;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class RunVisualizeReportCase extends AbstractSimpleUseCase<VisualizeComponent> {

    private final VisualizeComponent mVisualizeComponent;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;
    private final String mReportUri;

    @Inject
    public RunVisualizeReportCase(PreExecutionThread preExecutionThread,
                                  PostExecutionThread postExecutionThread,
                                  VisualizeComponent visualizeComponent,
                                  ReportParamsCache reportParamsCache,
                                  ReportParamsMapper reportParamsMapper,
                                  @Named("report_uri") String reportUri
    ) {
        super(preExecutionThread, postExecutionThread);
        mVisualizeComponent = visualizeComponent;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
        mReportUri = reportUri;
    }

    @Override
    protected Observable<VisualizeComponent> buildUseCaseObservable() {
        List<ReportParameter> reportParameters = mReportParamsCache.get(mReportUri);
        String jsonParams = mReportParamsMapper.toJsonLegacyParams(reportParameters);
        mVisualizeComponent.run(jsonParams);
        return Observable.just(mVisualizeComponent);
    }
}
