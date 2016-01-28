package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryResourceRepository implements ResourceRepository {
    private final RxRepositoryService mRepositoryService;
    private final ResourceMapper mResourceMapper;
    private Observable<AppResource> mGetReportDetailsAction;

    @Inject
    public InMemoryResourceRepository(RxRepositoryService repositoryService,
                                      ResourceMapper resourceMapper) {
        mRepositoryService = repositoryService;
        mResourceMapper = resourceMapper;
    }

    @Override
    public Observable<AppResource> getReportResource(@NonNull String reportUri) {
        if (mGetReportDetailsAction == null) {
            mGetReportDetailsAction = mRepositoryService.fetchReportDetails(reportUri)
                    .map(new Func1<ReportResource, AppResource>() {
                        @Override
                        public AppResource call(ReportResource reportResource) {
                            return mResourceMapper.mapReportResource(reportResource);
                        }
                    })
                    .doOnTerminate(new Action0() {
                        @Override
                        public void call() {
                            mGetReportDetailsAction = null;
                        }
                    }).cache();
        }

        return mGetReportDetailsAction;
    }
}
