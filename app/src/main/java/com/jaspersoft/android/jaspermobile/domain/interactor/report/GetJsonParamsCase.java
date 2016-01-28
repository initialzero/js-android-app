package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class GetJsonParamsCase extends AbstractUseCase<String, String> {

    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;

    @Inject
    public GetJsonParamsCase(PreExecutionThread preExecutionThread,
                             PostExecutionThread postExecutionThread,
                             ReportParamsCache reportParamsCache,
                             ReportParamsMapper reportParamsMapper
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
    }

    @Override
    protected Observable<String> buildUseCaseObservable(@NotNull String reportUri) {
        List<ReportParameter> reportParameters = mReportParamsCache.get(reportUri);
        String jsonParams = mReportParamsMapper.legacyParamsToJson(reportParameters);
        return Observable.just(jsonParams);
    }
}
