package com.jaspersoft.android.jaspermobile.domain.interactor.resource;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.ResourceDetailsRequest;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.repository.Resource;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetResourceDetailsByTypeCase extends AbstractUseCase<ResourceLookup, ResourceDetailsRequest> {
    private final ResourceRepository mResourceRepository;
    private final ResourceMapper mResourceMapper;

    @Inject
    public GetResourceDetailsByTypeCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ResourceRepository resourceRepository,
            ResourceMapper resourceMapper) {
        super(preExecutionThread, postExecutionThread);
        mResourceRepository = resourceRepository;
        mResourceMapper = resourceMapper;
    }

    @Override
    public Observable<ResourceLookup> buildUseCaseObservable(@NonNull final ResourceDetailsRequest request) {
        return mResourceRepository.getResourceByType(request.getUri(), request.getType())
                .flatMap(new Func1<Resource, Observable<ResourceLookup>>() {
                    @Override
                    public Observable<ResourceLookup> call(Resource resource) {
                        try {
                            ResourceLookup lookup = mResourceMapper.toConcreteLegacyResource(resource, request.getType());
                            return Observable.just(lookup);
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                });
    }
}
