package com.jaspersoft.android.jaspermobile.domain.interactor.resource;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class SearchResourcesCase extends AbstractUseCase<List<ResourceLookup>, ResourceLookupSearchCriteria> {

    private final ResourceRepository mResourceRepository;

    @Inject
    public SearchResourcesCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ResourceRepository resourceRepository) {
        super(preExecutionThread, postExecutionThread);
        mResourceRepository = resourceRepository;
    }

    @Override
    protected Observable<List<ResourceLookup>> buildUseCaseObservable(ResourceLookupSearchCriteria legacyCriteria) {
        return mResourceRepository.searchResources(legacyCriteria);
    }
}
