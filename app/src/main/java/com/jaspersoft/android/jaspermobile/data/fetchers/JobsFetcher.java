/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.fetchers;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.JobsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.JobsSortMapper;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.entity.job.JobResource;
import com.jaspersoft.android.jaspermobile.domain.entity.Resource;
import com.jaspersoft.android.jaspermobile.domain.model.JobResourceModel;
import com.jaspersoft.android.jaspermobile.domain.store.SearchQueryStore;
import com.jaspersoft.android.jaspermobile.domain.store.SortStore;
import com.jaspersoft.android.jaspermobile.internal.di.PerScreen;
import com.jaspersoft.android.sdk.service.data.schedule.JobUnit;
import com.jaspersoft.android.sdk.service.report.schedule.JobSearchCriteria;
import com.jaspersoft.android.sdk.service.report.schedule.JobSortType;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxJobSearchTask;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@PerScreen
public class JobsFetcher extends CatalogFetcherImpl<JobUnit, JobResource> {
    private final JasperRestClient mClient;
    private final SortStore mSortStore;
    private final SearchQueryStore mSearchQueryStore;
    private final JobsMapper mJobsMapper;
    private final JobsSortMapper mJobsSortMapper;
    private final JobResourceModel mJobResourceModel;

    private RxJobSearchTask mSearchTask;

    @Inject
    public JobsFetcher(
            JasperRestClient mClient,
            SortStore mSortStore,
            SearchQueryStore mSearchQueryStore,
            JobsMapper mJobsMapper,
            JobsSortMapper mJobsSortMapper,
            JobResourceModel mJobResourceModel) {
        this.mClient = mClient;
        this.mSortStore = mSortStore;
        this.mSearchQueryStore = mSearchQueryStore;
        this.mJobsMapper = mJobsMapper;
        this.mJobsSortMapper = mJobsSortMapper;
        this.mJobResourceModel = mJobResourceModel;

        mJobResourceModel.subscribeOnDeletion(new SimpleSubscriber<Integer>() {
            @Override
            public void onNext(Integer id) {
                for (Resource resource : getResourceList()) {
                    if (resource.getId() == id) {
                        getResourceList().remove(resource);
                        break;
                    }
                }
                getLoaderCallback().onLoaded(getResourceList());
            }
        });

        observe(mSearchQueryStore.observe());
    }

    @Override
    public void reset() {
        mSearchTask = null;
        mJobResourceModel.clear();
        super.reset();
    }

    @Override
    protected boolean searchTaskInitialized() {
        return mSearchTask != null;
    }

    @Override
    protected void createSearchTask() {
        JobSortType sortType = mJobsSortMapper.to(mSortStore.getSortType());
        JobSearchCriteria jobSearchCriteria = JobSearchCriteria.builder()
                .withLabel(mSearchQueryStore.getQuery())
                .withSortType(sortType)
                .build();

        mSearchTask = mClient.scheduleService().toBlocking().first().search(jobSearchCriteria);
    }

    @Override
    protected boolean hasNext() {
        return mSearchTask.hasNext();
    }

    @Override
    protected Observable<List<JobUnit>> getNextTask() {
        return mSearchTask.nextLookup();
    }

    @Override
    protected List<JobResource> map(List<JobUnit> items) {
        return mJobsMapper.toJobResources(items);
    }
}
