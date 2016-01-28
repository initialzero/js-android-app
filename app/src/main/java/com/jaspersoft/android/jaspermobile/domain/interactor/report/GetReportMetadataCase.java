package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;

/**
 * TODO: remove data package related dependencies
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportMetadataCase extends AbstractUseCase<AppResource, ReportData> {

    private final ResourceRepository mResourceRepository;
    private final ReportParamsCache mReportParamsCache;
    private final ReportParamsMapper mReportParamsMapper;

    @Inject
    public GetReportMetadataCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ResourceRepository resourceRepository,
            ReportParamsCache reportParamsCache,
            ReportParamsMapper reportParamsMapper
        ) {
        super(preExecutionThread, postExecutionThread);
        mResourceRepository = resourceRepository;
        mReportParamsCache = reportParamsCache;
        mReportParamsMapper = reportParamsMapper;
    }

    @Override
    protected Observable<AppResource> buildUseCaseObservable(final ReportData reportData) {
        return Observable.defer(new Func0<Observable<AppResource>>() {
            @Override
            public Observable<AppResource> call() {
                List<ReportParameter> reportParameters =
                        mReportParamsMapper.mapToLegacyParams(reportData.getParams());
                String uri = reportData.getResource();
                mReportParamsCache.put(uri, reportParameters);
                return mResourceRepository.getReportResource(uri);
            }
        });
    }
}
