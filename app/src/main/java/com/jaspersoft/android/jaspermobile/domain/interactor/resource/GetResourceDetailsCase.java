package com.jaspersoft.android.jaspermobile.domain.interactor.resource;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class GetResourceDetailsCase extends AbstractUseCase<ResourceLookup, String> {
    private final ResourceRepository mResourceRepository;
    private final ResourceMapper mResourceMapper;

    @Inject
    public GetResourceDetailsCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ResourceRepository resourceRepository,
            ResourceMapper resourceMapper
    ) {
        super(preExecutionThread, postExecutionThread);
        mResourceRepository = resourceRepository;
        mResourceMapper = resourceMapper;
    }

    @Override
    protected Observable<ResourceLookup> buildUseCaseObservable(String uri) {
        return mResourceRepository.getReportResource(uri)
                .map(new Func1<ReportResource, ResourceLookup>() {
                    @Override
                    public ResourceLookup call(ReportResource resource) {
                        ResourceLookup lookup = new ResourceLookup();
                        mResourceMapper.toLegacyResource(resource, lookup);
                        return lookup;
                    }
                });
    }
}
