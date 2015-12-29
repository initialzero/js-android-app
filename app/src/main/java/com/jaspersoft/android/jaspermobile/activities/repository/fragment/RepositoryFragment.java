/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.SimpleScrollListener;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.filtering.RepositoryResourceFilter;
import com.jaspersoft.android.jaspermobile.util.multichoice.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.util.resource.pagination.Emerald2PaginationFragment_;
import com.jaspersoft.android.jaspermobile.util.resource.pagination.Emerald3PaginationFragment_;
import com.jaspersoft.android.jaspermobile.util.resource.pagination.PaginationPolicy;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetRootFolderDataRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;

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
public class RepositoryFragment extends RoboSpiceFragment
        implements SwipeRefreshLayout.OnRefreshListener, ResourceAdapter.ResourceInteractionListener {

    public static final String TAG = RepositoryFragment.class.getSimpleName();
    public static final String ROOT_URI = "/";
    // Loader actions
    private static final int LOAD_FROM_CACHE = 1;
    private static final int LOAD_FROM_NETWORK = 2;

    @InjectView(android.R.id.list)
    protected AbsListView listView;
    @InjectView(R.id.refreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @InjectView(android.R.id.empty)
    protected TextView emptyText;

    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected ResourceLookupSearchCriteria mSearchCriteria;
    @Inject
    protected Analytics analytics;

    @InstanceState
    @FragmentArg
    String prefTag;
    @InstanceState
    @FragmentArg
    protected String resourceLabel;
    @InstanceState
    @FragmentArg
    protected String resourceUri;
    @InstanceState
    @FragmentArg
    protected String query;
    @InstanceState
    @FragmentArg
    protected boolean recursive;

    @FragmentArg
    protected ViewType viewType;

    @Inject
    @Named("LIMIT")
    protected int mLimit;
    @Inject
    @Named("THRESHOLD")
    protected int mTreshold;

    @InstanceState
    protected boolean mLoading;
    @InstanceState
    protected int mLoaderState = LOAD_FROM_CACHE;

    @Bean
    RepositoryResourceFilter repositoryResourceFilter;
    @Bean
    protected DefaultPrefHelper prefHelper;
    @Bean
    protected ResourceOpener resourceOpener;
    @Bean
    protected FavoritesHelper favoritesHelper;

    private ResourceAdapter mAdapter;
    private PaginationPolicy mPaginationPolicy;
    private AccountServerData mServerData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchCriteria.setForceFullPage(true);
        mSearchCriteria.setLimit(mLimit);
        mSearchCriteria.setRecursive(recursive);
        mSearchCriteria.setTypes(repositoryResourceFilter.getCurrent().getValues());
        mSearchCriteria.setFolderUri(TextUtils.isEmpty(resourceUri) ? ROOT_URI : resourceUri);
        if (!TextUtils.isEmpty(query)) {
            mSearchCriteria.setQuery(query);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                (viewType == ViewType.LIST) ? R.layout.fragment_resources_list : R.layout.fragment_resources_grid,
                container, false);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        mServerData = AccountServerData.get(getActivity(), account);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.js_blue,
                R.color.js_dark_blue,
                R.color.js_blue,
                R.color.js_dark_blue);

        listView.setOnScrollListener(new ScrollListener());
        setDataAdapter(savedInstanceState);
        updatePaginationPolicy();
        loadPage();
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean isResourceLoaded = (mAdapter.getCount() == 0);
        if (!mLoading && isResourceLoaded) {
            loadPage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (TextUtils.isEmpty(resourceLabel)) {
                actionBar.setTitle(getString(R.string.h_repository_label));
            } else {
                actionBar.setTitle(resourceLabel);
            }
        }

        List<Analytics.Dimension> viewDimension = new ArrayList<>();
        viewDimension.add(new Analytics.Dimension(Analytics.Dimension.RESOURCE_VIEW_HIT_KEY, viewType.name()));
        analytics.sendScreenView(Analytics.ScreenName.REPOSITORY.getValue(), viewDimension);
    }

    @Override
    public void onPause() {
        swipeRefreshLayout.clearAnimation();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        listView.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAdapter != null) {
            mAdapter.save(outState);
        }
        super.onSaveInstanceState(outState);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(ResourceLookup resource) {
        mAdapter.finishActionMode();
        resourceOpener.openResource(this, prefTag, resource);
    }

    //---------------------------------------------------------------------
    // Implements SwipeRefreshLayout.OnRefreshListener
    //---------------------------------------------------------------------

    @Override
    public void onRefresh() {
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();
        mLoaderState = LOAD_FROM_NETWORK;
        loadPage();

        analytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.REFRESHED.getValue(), Analytics.EventLabel.REPOSITORY.getValue());
    }

    //---------------------------------------------------------------------
    // Implements ResourcesLoader
    //---------------------------------------------------------------------

    public void loadFirstPage() {
        mSearchCriteria.setOffset(0);
        loadResources(mLoaderState);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void setDataAdapter(Bundle savedInstanceState) {
        mAdapter = new ResourceAdapter(getActivity(), savedInstanceState, viewType);
        mAdapter.setResourcesInteractionListener(this);
        mAdapter.setAdapterView(listView);
        listView.setAdapter(mAdapter);
    }

    private void updatePaginationPolicy() {
        ServerRelease release = ServerRelease.parseVersion(mServerData.getVersionName());
        double versionCode = release.code();

        if (versionCode <= ServerRelease.EMERALD_MR2.code()) {
            mPaginationPolicy = Emerald2PaginationFragment_.builder().build();
        }
        if (versionCode > ServerRelease.EMERALD_MR2.code()) {
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

    private void loadRootFolders() {
        setRefreshState(true);
        showEmptyText(R.string.loading_msg);
        // Fetch default URI
        GetRootFolderDataRequest request = new GetRootFolderDataRequest(jsRestClient);
        long cacheExpiryDuration = (LOAD_FROM_CACHE == mLoaderState)
                ? prefHelper.getRepoCacheExpirationValue() : DurationInMillis.ALWAYS_EXPIRED;
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration,
                new GetRootFolderDataRequestListener());
    }

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
        if (!refreshing) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadPage() {
        boolean isRoot = TextUtils.isEmpty(resourceUri);
        boolean isProJrs = mServerData.getEdition().equals("PRO");
        if (isRoot && isProJrs) {
            loadRootFolders();
        } else {
            loadFirstPage();
        }
    }

    //---------------------------------------------------------------------
    // Implements FavoritesAdapter.FavoritesInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onFavorite(ResourceLookup resource) {
        Uri uri = favoritesHelper.queryFavoriteUri(resource);
        favoritesHelper.handleFavoriteMenuAction(uri, resource, null);
    }

    @Override
    public void onInfo(String resourceTitle, String resourceDescription) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SimpleDialogFragment.createBuilder(getActivity(), fm)
                .setTitle(resourceTitle)
                .setMessage(resourceDescription)
                .setNegativeButtonText(R.string.ok)
                .show();
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class GetRootFolderDataRequestListener extends SimpleRequestListener<FolderDataResponse> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            setRefreshState(false);
            showEmptyText(R.string.failed_load_data);
        }

        @Override
        public void onRequestSuccess(FolderDataResponse folderDataResponse) {
            // Do this for explicit refresh during pull to refresh interaction
            if (mLoaderState == LOAD_FROM_NETWORK) {
                mAdapter.setNotifyOnChange(false);
                mAdapter.clear();
            }

            mAdapter.add(folderDataResponse);

            ResourceLookup publicLookup = new ResourceLookup();
            publicLookup.setResourceType(ResourceLookup.ResourceType.folder);
            publicLookup.setLabel("Public");
            publicLookup.setUri("/public");
            mAdapter.add(publicLookup);

            mAdapter.setNotifyOnChange(true);
            mAdapter.notifyDataSetChanged();

            setRefreshState(false);
            showEmptyText(R.string.resources_not_found);
        }
    }

    private class GetResourceLookupsListener extends SimpleRequestListener<ResourceLookupsList> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
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
            mAdapter.addAllByType(datum);
            mAdapter.setNotifyOnChange(true);
            mAdapter.notifyDataSetChanged();

            // set refresh states
            setRefreshState(false);
            // If need we show 'empty' message
            showEmptyText(R.string.resources_not_found);
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

                analytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.LOADED_NEXT.getValue(),  Analytics.EventLabel.REPOSITORY.getValue());
            }
            enableRefreshLayout(listView);
        }

        private void enableRefreshLayout(AbsListView listView) {
            boolean enable = true;
            if (listView != null && listView.getChildCount() > 0) {
                // check if the first item of the list is visible
                boolean firstItemVisible = listView.getFirstVisiblePosition() == 0;

                // check if the top of the first item is visible
                View topViewItem = listView.getChildAt(0);
                boolean topOfFirstItemVisible = topViewItem.getTop() - listView.getPaddingTop() == 0;

                // enabling or disabling the refresh layout
                enable = firstItemVisible && topOfFirstItemVisible;
            }
            swipeRefreshLayout.setEnabled(enable);
        }
    }

}
