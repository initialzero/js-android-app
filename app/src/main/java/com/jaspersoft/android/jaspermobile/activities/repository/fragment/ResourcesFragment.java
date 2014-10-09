/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import android.app.ActionBar;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ResourcesLoader;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ResourcesFragment extends RoboSpiceFragment
        implements AbsListView.OnScrollListener,
        SwipeRefreshLayout.OnRefreshListener,
        ResourcesLoader {

    public static final String ROOT_URI = "/";
    // Loader actions
    private static final int LOAD_FROM_CACHE = 1;
    private static final int LOAD_FROM_NETWORK = 2;
    // Context menu actions
    private static final int ID_CM_FAVORITE = 10;

    @InjectView(android.R.id.list)
    AbsListView listView;
    @InjectView(R.id.refreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @InjectView(android.R.id.empty)
    TextView emptyText;

    @Inject
    JsRestClient jsRestClient;
    @Inject
    ResourceLookupSearchCriteria mSearchCriteria;

    @InstanceState
    @FragmentArg
    ArrayList<String> resourceTypes;
    @InstanceState
    @FragmentArg
    SortOrder sortOrder;
    @InstanceState
    @FragmentArg
    boolean recursiveLookup;
    @InstanceState
    @FragmentArg
    String resourceLabel;
    @InstanceState
    @FragmentArg
    String resourceUri;
    @InstanceState
    @FragmentArg
    String query;
    @InstanceState
    @FragmentArg
    int emptyMessage;

    @FragmentArg
    ViewType viewType;

    @Inject
    @Named("LIMIT")
    int mLimit;
    @Inject
    @Named("THRESHOLD")
    int mTreshold;

    @InstanceState
    boolean mLoading;
    @InstanceState
    int mLoaderState = LOAD_FROM_CACHE;

    @Bean
    DefaultPrefHelper prefHelper;
    @Bean
    ResourceOpener resourceOpener;
    @Bean
    FavoritesHelper favoritesHelper;

    private int mTotal;
    private ResourceAdapter mAdapter;
    private final DataObservable mObservable = new DataObservable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resourceOpener.setResourceTypes(resourceTypes);

        mSearchCriteria.setRecursive(recursiveLookup);
        mSearchCriteria.setTypes(resourceTypes);
        mSearchCriteria.setFolderUri(TextUtils.isEmpty(resourceUri) ? ROOT_URI : resourceUri);
        if (!TextUtils.isEmpty(query)) {
            mSearchCriteria.setQuery(query);
        }
        if (sortOrder != null) {
            mSearchCriteria.setSortBy(sortOrder.getValue());
        }

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(TextUtils.isEmpty(resourceLabel) ?
                    getActivity().getTitle() : resourceLabel);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                (viewType == ViewType.LIST) ? R.layout.fragment_resources_list : R.layout.fragment_resources_grid,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_light,
                R.color.holo_blue_dark,
                R.color.holo_blue_light,
                R.color.holo_blue_bright);

        listView.setOnScrollListener(this);

        mAdapter = ResourceAdapter.builder(getActivity(), savedInstanceState)
                .setViewType(viewType)
                .create();
        mAdapter.setAdapterView(listView);
        mAdapter.registerDataSetObserver(mObservable);
        listView.setAdapter(mAdapter);

        loadFirstPage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
        super.onSaveInstanceState(outState);
    }

    public void loadFirstPage() {
        mSearchCriteria.setOffset(0);
        mSearchCriteria.setLimit(mLimit);
        loadResources(mLoaderState);
    }

    public boolean isLoading() {
        return mLoading;
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(ResourceLookup resource) {
        mAdapter.finishActionMode();
        resourceOpener.openResource(resource);
    }

    //---------------------------------------------------------------------
    // Implements AbsListView.OnScrollListener
    //---------------------------------------------------------------------

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount - mTreshold) {
            loadNextPage();
        }

        boolean enable = false;
        if (listView != null && listView.getChildCount() > 0) {
            // check if the first item of the list is visible
            boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;
            // check if the top of the first item is visible
            boolean topOfFirstItemVisible = listView.getChildAt(0).getTop() == 0;
            // enabling or disabling the refresh layout
            enable = firstItemVisible && topOfFirstItemVisible;
        }
        swipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    //---------------------------------------------------------------------
    // Implements SwipeRefreshLayout.OnRefreshListener
    //---------------------------------------------------------------------

    @Override
    public void onRefresh() {
        mLoaderState = LOAD_FROM_NETWORK;
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        loadFirstPage();
    }

    //---------------------------------------------------------------------
    // Implements ResourcesLoader
    //---------------------------------------------------------------------

    @Override
    public void loadResourcesByTypes(List<String> types) {
        resourceTypes = Lists.newArrayList(types);
        mSearchCriteria.setTypes(resourceTypes);
        mAdapter.clear();
        loadFirstPage();
    }

    @Override
    public void loadResourcesBySortOrder(SortOrder order) {
        sortOrder = order;
        mSearchCriteria.setSortBy(order.getValue());
        mAdapter.clear();
        loadFirstPage();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadNextPage() {
        if (!mLoading && hasNextPage()) {
            mSearchCriteria.setOffset(mSearchCriteria.getOffset() + mLimit);
            mLoaderState = LOAD_FROM_CACHE;
            loadResources(mLoaderState);
        }
    }

    private boolean hasNextPage() {
        return mSearchCriteria.getOffset() + mLimit < mTotal;
    }

    private void loadResources(int state) {
        mLoading = true;
        swipeRefreshLayout.setRefreshing(true);
        showEmptyText(R.string.loading_msg);

        GetResourceLookupsRequest request = new GetResourceLookupsRequest(jsRestClient, mSearchCriteria);
        long cacheExpiryDuration = (LOAD_FROM_CACHE == state)
                ? prefHelper.getRepoCacheExpirationValue() : DurationInMillis.ALWAYS_EXPIRED;
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceLookupsListener());
    }


    public void showEmptyText(int resId) {
        emptyText.setVisibility((listView.getChildCount() > 0) ? View.GONE : View.VISIBLE);
        if (resId != 0) emptyText.setText(resId);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private class GetResourceLookupsListener implements RequestListener<ResourceLookupsList> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), true);
            mLoading = false;
            showEmptyText(R.string.failed_load_data);
        }

        @Override
        public void onRequestSuccess(ResourceLookupsList resourceLookupsList) {
            boolean isFirstPage = mSearchCriteria.getOffset() == 0;
            showEmptyText(emptyMessage);

            if (isFirstPage) {
                mTotal = resourceLookupsList.getTotalCount();
            }

            List<ResourceLookup> datum = resourceLookupsList.getResourceLookups();
            Collections.sort(datum, new OrderingByType());
            mAdapter.setNotifyOnChange(true);
            mAdapter.addAll(datum);
        }
    }

    private class DataObservable extends DataSetObserver {
        public void onChanged() {
            super.onChanged();
            mLoading = false;
            swipeRefreshLayout.setRefreshing(false);
            emptyText.setVisibility((mAdapter.getCount() > 0) ? View.GONE : View.VISIBLE);
        }
    }

    private static class OrderingByType extends Ordering<ResourceLookup> {
        @Override
        public int compare(ResourceLookup res1, ResourceLookup res2) {
            ResourceLookup.ResourceType resType1 = res1.getResourceType();
            ResourceLookup.ResourceType resType2 = res2.getResourceType();
            return Ints.compare(resType1.ordinal(), resType2.ordinal());
        }
    }

}
