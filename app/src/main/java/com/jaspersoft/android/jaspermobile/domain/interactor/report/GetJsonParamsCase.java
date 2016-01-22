package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
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
public class GetJsonParamsCase extends AbstractSimpleUseCase<String> {

    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;
    private final String mReportUri;

    @Inject
    public GetJsonParamsCase(PreExecutionThread preExecutionThread,
                             PostExecutionThread postExecutionThread,
                             ReportParamsCache reportParamsCache,
                             ReportParamsMapper reportParamsMapper,
                             @Named("report_uri") String reportUri
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
        mReportUri = reportUri;
    }

    @Override
    protected Observable<String> buildUseCaseObservable() {
        List<ReportParameter> reportParameters = mReportParamsCache.get(mReportUri);
        String jsonParams = mReportParamsMapper.toJsonLegacyParams(reportParameters);
        return Observable.just(jsonParams);
    }
}
