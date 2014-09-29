/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.support.IResourcesLoader;
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
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        IResourcesLoader {

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

        registerForContextMenu(listView);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_blue_bright);

        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        if (mAdapter == null) {
            mAdapter = ResourceAdapter.builder(getActivity())
                    .setViewType(viewType)
                    .create();
            mAdapter.registerDataSetObserver(mObservable);
            listView.setAdapter(mAdapter);
        }

        loadFirstPage();
    }


    public void loadFirstPage() {
        mSearchCriteria.setOffset(0);
        mSearchCriteria.setLimit(mLimit);
        loadResources(mLoaderState);
    }

    public boolean isLoading() {
        return mLoading;
    }

    //---------------------------------------------------------------------
    // Implements Context Menu
    //---------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        // Determine on which item in the ListView the user long-clicked
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ResourceLookup resource = mAdapter.getItem(info.position);
        Uri uri = favoritesHelper.queryFavoriteUri(resource);

        // Retrieve the label for that particular item and use it as title for the menu
        menu.setHeaderTitle(resource.getLabel());

        // Add all the menu options
        menu.add(Menu.NONE, ID_CM_FAVORITE, Menu.NONE, (uri == null) ?
                R.string.r_cm_add_to_favorites : R.string.r_cm_remove_from_favorites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Determine on which item in the ListView the user long-clicked and get it from Cursor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Handle item selection
        switch (item.getItemId()) {
            case ID_CM_FAVORITE:
                ResourceLookup resource = mAdapter.getItem(info.position);
                Uri uri = favoritesHelper.queryFavoriteUri(resource);
                favoritesHelper.handleFavoriteMenuAction(uri, resource, null);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Implements AbsListView.OnItemClickListener
    //---------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ResourceLookup resource = (ResourceLookup) listView.getItemAtPosition(position);
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
    // Implements IResourcesLoader
    //---------------------------------------------------------------------

    @Override
    public void loadResourcesByTypes(List<String> types) {
        resourceTypes = Lists.newArrayList(types);
        mSearchCriteria.setTypes(resourceTypes);
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
