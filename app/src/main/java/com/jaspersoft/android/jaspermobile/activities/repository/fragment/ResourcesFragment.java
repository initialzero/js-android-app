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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
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
import com.jaspersoft.android.jaspermobile.util.SimpleScrollListener;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetRootFolderDataRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ResourcesFragment extends RoboSpiceFragment
        implements SwipeRefreshLayout.OnRefreshListener, ResourcesLoader {

    public static final String ROOT_URI = "/";
    // Loader actions
    private static final int LOAD_FROM_CACHE = 1;
    private static final int LOAD_FROM_NETWORK = 2;

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

    private ResourceAdapter mAdapter;
    private PaginationPolicy mPaginationPolicy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resourceOpener.setResourceTypes(resourceTypes);

        mSearchCriteria.setForceFullPage(true);
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

        listView.setOnScrollListener(new ScrollListener());

        mAdapter = ResourceAdapter.builder(getActivity(), savedInstanceState)
                .setViewType(viewType)
                .create();
        mAdapter.setAdapterView(listView);
        listView.setAdapter(mAdapter);

        fetchServerInfo();
    }

    private void fetchServerInfo() {
        showEmptyText(R.string.loading_msg);
        setRefreshState(true);
        GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
        getSpiceManager().execute(request, new GetServerInfoListener());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
        super.onSaveInstanceState(outState);
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(ResourceLookup resource) {
        mAdapter.finishActionMode();
        resourceOpener.openResource(resource);
    }

    //---------------------------------------------------------------------
    // Implements SwipeRefreshLayout.OnRefreshListener
    //---------------------------------------------------------------------

    @Override
    public void onRefresh() {
        mLoaderState = LOAD_FROM_NETWORK;
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

    public void loadFirstPage() {
        mSearchCriteria.setOffset(0);
        loadResources(mLoaderState);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadNextPage() {
        if (!mLoading && hasNextPage()) {
            mSearchCriteria.setOffset(calculateNextOffset());
            mLoaderState = LOAD_FROM_CACHE;
            loadResources(mLoaderState);
        }
    }

    private boolean hasNextPage() {
        return mPaginationPolicy.hasNextPage();
    }

    private int calculateNextOffset() {
        return mPaginationPolicy.calculateNextOffset();
    }

    private void loadResources(int state) {
        setRefreshState(true);
        showEmptyText(R.string.loading_msg);

        GetResourceLookupsRequest request = new GetResourceLookupsRequest(jsRestClient, mSearchCriteria);
        long cacheExpiryDuration = (LOAD_FROM_CACHE == state)
                ? prefHelper.getRepoCacheExpirationValue() : DurationInMillis.ALWAYS_EXPIRED;
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceLookupsListener());
    }

    private void showEmptyText(int resId) {
        boolean noItems = (mAdapter.getCount() > 0);
        emptyText.setVisibility(noItems ? View.GONE : View.VISIBLE);
        if (resId != 0) emptyText.setText(resId);
    }

    private void setRefreshState(boolean refreshing) {
        mLoading = refreshing;
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class GetServerInfoListener implements RequestListener<ServerInfo> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), true);
            setRefreshState(false);
            showEmptyText(R.string.failed_load_data);
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            setUpPaginationPolicy(serverInfo);

            String proVersion = ServerInfo.EDITIONS.PRO;
            boolean isRepository = !recursiveLookup;
            boolean isRoot = TextUtils.isEmpty(resourceUri);
            boolean isProJrs = proVersion.equals(serverInfo.getEdition());
            if (isRepository && isRoot && isProJrs) {
                // Fetch default URI
                GetRootFolderDataRequest request = new GetRootFolderDataRequest(jsRestClient);
                long cacheExpiryDuration = (LOAD_FROM_CACHE == mLoaderState)
                        ? prefHelper.getRepoCacheExpirationValue() : DurationInMillis.ALWAYS_EXPIRED;
                getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration,
                        new GetRootFolderDataRequestListener());
            } else {
                loadFirstPage();
            }
        }

        protected void setUpPaginationPolicy(ServerInfo serverInfo) {
            double versionCode = serverInfo.getVersionCode();
            if (versionCode <= ServerInfo.VERSION_CODES.EMERALD_TWO) {
                mPaginationPolicy = Emerald2PaginationFragment_.builder().build();
            }
            if (versionCode > ServerInfo.VERSION_CODES.EMERALD_TWO) {
                mPaginationPolicy = Emerald3PaginationFragment_.builder().build();
            }

            if (mPaginationPolicy == null) {
                throw new UnsupportedOperationException();
            } else {
                mPaginationPolicy.setSearchCriteria(mSearchCriteria);
                getChildFragmentManager().beginTransaction()
                        .add((Fragment) mPaginationPolicy,
                                PaginationPolicy.class.getSimpleName()).commit();
            }
        }
    }

    private class GetRootFolderDataRequestListener implements RequestListener<FolderDataResponse> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), true);
            setRefreshState(false);
            showEmptyText(R.string.failed_load_data);
        }

        @Override
        public void onRequestSuccess(FolderDataResponse folderDataResponse) {
            mAdapter.add(folderDataResponse);

            ResourceLookup publicLookup = new ResourceLookup();
            publicLookup.setResourceType(ResourceLookup.ResourceType.folder);
            publicLookup.setLabel("Public");
            publicLookup.setUri("/public");
            mAdapter.add(publicLookup);

            setRefreshState(false);
            showEmptyText(emptyMessage);
        }
    }
    
    private class GetResourceLookupsListener implements RequestListener<ResourceLookupsList> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, getActivity(), true);
            setRefreshState(false);
            showEmptyText(R.string.failed_load_data);
        }

        @Override
        public void onRequestSuccess(ResourceLookupsList resourceLookupsList) {
            // set pagination data
            mPaginationPolicy.handleLookup(resourceLookupsList);

            // set data
            List<ResourceLookup> datum = resourceLookupsList.getResourceLookups();
            // Do this for explicit refresh during pull to refresh interaction
            if (mLoaderState == LOAD_FROM_NETWORK) {
                mAdapter.setNotifyOnChange(false);
                mAdapter.clear();
            }
            mAdapter.addAll(datum);
            // We won`t sort by type in Library section
            if (resourceTypes != null &&
                    resourceTypes.contains(ResourceLookup.ResourceType.folder.toString())) {
                mAdapter.sortByType();
            }
            mAdapter.setNotifyOnChange(true);
            mAdapter.notifyDataSetChanged();

            // set refresh states
            setRefreshState(false);
            // If need we show 'empty' message
            showEmptyText(emptyMessage);
        }
    }

    //---------------------------------------------------------------------
    // Implements AbsListView.OnScrollListener
    //---------------------------------------------------------------------

    private class ScrollListener extends SimpleScrollListener {
        @Override
        public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount - mTreshold) {
                loadNextPage();
            }
            enableRefreshLayout(listView);
        }

        private void enableRefreshLayout(AbsListView listView) {
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
    }

}
