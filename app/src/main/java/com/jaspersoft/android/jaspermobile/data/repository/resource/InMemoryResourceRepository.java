package com.jaspersoft.android.jaspermobile.data.repository.resource;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.CriteriaMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.SearchResult;
import com.jaspersoft.android.jaspermobile.domain.repository.resource.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.service.data.report.ResourceOutput;
import com.jaspersoft.android.sdk.service.data.repository.Resource;
import com.jaspersoft.android.sdk.service.data.repository.ResourceType;
import com.jaspersoft.android.sdk.service.repository.RepositorySearchCriteria;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import java.util.List;

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
    private final JasperRestClient mRestClient;
    private final CriteriaMapper mCriteriaMapper;
    private final ResourceMapper mResourceMapper;

    @Inject
    public InMemoryResourceRepository(
            JasperRestClient restClient,
            CriteriaMapper criteriaMapper,
            ResourceMapper resourceMapper
    ) {
        mRestClient = restClient;
        mCriteriaMapper = criteriaMapper;
        mResourceMapper = resourceMapper;
    }

    @NonNull
    @Override
    public Observable<Resource> getResourceByType(@NonNull final String reportUri, @NonNull final String type) {
        return mRestClient.repositoryService()
                .flatMap(new Func1<RxRepositoryService, Observable<Resource>>() {
                    @Override
                    public Observable<Resource> call(RxRepositoryService service) {
                        return service.fetchResourceDetails(reportUri, ResourceType.valueOf(type));
                    }
                });
    }

    @NonNull
    @Override
    public Observable<ResourceOutput> getResourceContent(@NonNull final String resourceUri) {
        return mRestClient.repositoryService()
                .flatMap(new Func1<RxRepositoryService, Observable<ResourceOutput>>() {
                    @Override
                    public Observable<ResourceOutput> call(RxRepositoryService service) {
                        return service.fetchResourceContent(resourceUri);
                    }
                });
    }

    @NonNull
    @Override
    public Observable<SearchResult> searchResources(@NonNull final ResourceLookupSearchCriteria criteria) {
        return mRestClient.repositoryService()
                .flatMap(new Func1<RxRepositoryService, Observable<List<Resource>>>() {
                    @Override
                    public Observable<List<Resource>> call(RxRepositoryService service) {
                        RepositorySearchCriteria searchCriteria = mCriteriaMapper.toRetrofittedCriteria(criteria);
                        return service.search(searchCriteria).nextLookup();
                    }
                })
                .map(new Func1<List<Resource>, SearchResult>() {
                    @Override
                    public SearchResult call(List<Resource> resources) {
                        int limit = criteria.getLimit();
                        int searchCount = resources.size();
                        boolean hasReachedEnd = searchCount < limit;

                        List<ResourceLookup> lookups = mResourceMapper.toLegacyResources(resources);
                        SearchResult searchResult = new SearchResult(lookups, hasReachedEnd);
                        return searchResult;
                    }
                });
    }

    @NonNull
    @Override
    public Observable<List<FolderDataResponse>> getRootRepositories() {
        return mRestClient.repositoryService()
                .flatMap(new Func1<RxRepositoryService, Observable<List<Resource>>>() {
                    @Override
                    public Observable<List<Resource>> call(RxRepositoryService service) {
                        return service.fetchRootFolders();
                    }
                })
                .map(new Func1<List<Resource>, List<FolderDataResponse>>() {
                    @Override
                    public List<FolderDataResponse> call(List<Resource> resources) {
                        return mResourceMapper.toLegacyFolders(resources);
                    }
                });
    }
}
