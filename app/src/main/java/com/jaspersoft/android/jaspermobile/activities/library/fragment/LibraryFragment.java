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

package com.jaspersoft.android.jaspermobile.activities.library.fragment;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.filtering.LibraryResourceFilter;
import com.jaspersoft.android.jaspermobile.util.resource.pagination.Emerald2PaginationFragment_;
import com.jaspersoft.android.jaspermobile.util.resource.pagination.Emerald3PaginationFragment_;
import com.jaspersoft.android.jaspermobile.util.resource.pagination.PaginationPolicy;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceAdapter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceConverter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.SelectionModeHelper;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;
import com.jaspersoft.android.jaspermobile.widget.JasperRecyclerView;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.fragment_refreshable_resource)
public class LibraryFragment extends RoboSpiceFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = LibraryFragment.class.getSimpleName();
    public static final String ROOT_URI = "/";
    // Loader actions
    private static final int LOAD_FROM_CACHE = 1;
    private static final int LOAD_FROM_NETWORK = 2;

    @InjectView(android.R.id.list)
    protected JasperRecyclerView listView;
    @InjectView(R.id.refreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @InjectView(android.R.id.empty)
    protected TextView emptyText;

    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected ResourceLookupSearchCriteria mSearchCriteria;

    @InstanceState
    @FragmentArg
    protected String resourceLabel;
    @InstanceState
    @FragmentArg
    protected SortOrder sortOrder;
    @InstanceState
    @FragmentArg
    protected String query;

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
    protected LibraryResourceFilter libraryResourceFilter;
    @Bean
    protected DefaultPrefHelper prefHelper;
    @Bean
    protected ResourceOpener resourceOpener;
    @Bean
    protected FavoritesHelper favoritesHelper;

    private SelectionModeHelper mSelectionModeHelper;
    private JasperResourceAdapter mAdapter;
    private PaginationPolicy mPaginationPolicy;
    private AccountServerData mServerData;
    private HashMap<String, ResourceLookup> mResourceLookupHashMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResourceLookupHashMap = new HashMap<>();

        mSearchCriteria.setForceFullPage(true);
        mSearchCriteria.setLimit(mLimit);
        mSearchCriteria.setRecursive(true);
        mSearchCriteria.setTypes(libraryResourceFilter.getCurrent().getValues());
        mSearchCriteria.setFolderUri(ROOT_URI);
        if (!TextUtils.isEmpty(query)) {
            mSearchCriteria.setQuery(query);
        }
        if (sortOrder != null) {
            mSearchCriteria.setSortBy(sortOrder.getValue());
        }
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

        listView.addOnScrollListener(new ScrollListener());
        setDataAdapter();
        updatePaginationPolicy();
        loadFirstPage();
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean isResourceLoaded = (mAdapter.getItemCount() == 0);
        if (!mLoading && isResourceLoaded) {
            loadFirstPage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (TextUtils.isEmpty(resourceLabel)) {
                actionBar.setTitle(getString(R.string.h_library_label));
            } else {
                actionBar.setTitle(resourceLabel);
            }
        }
    }

    @Override
    public void onPause() {
        swipeRefreshLayout.clearAnimation();
        super.onPause();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    //---------------------------------------------------------------------
    // Implements SwipeRefreshLayout.OnRefreshListener
    //---------------------------------------------------------------------

    @Override
    public void onRefresh() {
        clearData();
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();
        mLoaderState = LOAD_FROM_NETWORK;
        loadFirstPage();
    }

    //---------------------------------------------------------------------
    // Implements ResourcesLoader
    //---------------------------------------------------------------------

    public void loadResourcesByTypes() {
        mSearchCriteria.setTypes(libraryResourceFilter.getCurrent().getValues());
        clearData();
        loadFirstPage();
    }

    public void loadResourcesBySortOrder(SortOrder order) {
        sortOrder = order;
        mSearchCriteria.setSortBy(order.getValue());
        clearData();
        loadFirstPage();
    }

    public void loadFirstPage() {
        mSearchCriteria.setOffset(0);
        loadResources(mLoaderState);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void clearData() {
        mResourceLookupHashMap.clear();
        mAdapter.clear();
        mSelectionModeHelper.finishSelectionMode();
    }

    private void addData(List<ResourceLookup> data) {
        JasperResourceConverter jasperResourceConverter = new JasperResourceConverter(getActivity());
        mResourceLookupHashMap.putAll(jasperResourceConverter.convertToDataMap(data));
        mAdapter.addAll(jasperResourceConverter.convertToJasperResource(data));
        mAdapter.notifyDataSetChanged();
    }

    private void onViewSingleClick(ResourceLookup resource) {
        if (mSelectionModeHelper != null) {
            mSelectionModeHelper.finishSelectionMode();
        }
        resourceOpener.openResource(this, resource);
    }

    private void setDataAdapter() {
        JasperResourceConverter jasperResourceConverter = new JasperResourceConverter(getActivity());

        List<ResourceLookup> resourceLookupList = null;
        mAdapter = new JasperResourceAdapter(jasperResourceConverter.convertToJasperResource(resourceLookupList), viewType);
        mAdapter.setOnItemInteractionListener(new JasperResourceAdapter.OnResourceInteractionListener() {
            @Override
            public void onResourceItemClicked(String id) {
                onViewSingleClick(mResourceLookupHashMap.get(id));
            }
        });

        listView.setViewType(viewType);
        listView.setAdapter(mAdapter);
        mSelectionModeHelper = new LibrarySelectionModeHelper(mAdapter);
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

        final GetResourceLookupsRequest request = new GetResourceLookupsRequest(jsRestClient, mSearchCriteria);
        final long cacheExpiryDuration = (LOAD_FROM_CACHE == state)
                ? prefHelper.getRepoCacheExpirationValue() : DurationInMillis.ALWAYS_EXPIRED;
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceLookupsListener());
    }

    private void showEmptyText(int resId) {
        boolean noItems = (mAdapter.getItemCount() > 0);
        emptyText.setVisibility(noItems ? View.GONE : View.VISIBLE);
        if (resId != 0) emptyText.setText(resId);
    }

    private void setRefreshState(boolean refreshing) {
        mLoading = refreshing;
        if (!refreshing) {
            swipeRefreshLayout.setRefreshing(false);
            mAdapter.hideLoading();
        } else {
            mAdapter.showLoading();
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

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

            addData(resourceLookupsList.getResourceLookups());

            // set refresh states
            setRefreshState(false);
            // If need we show 'empty' message
            showEmptyText(R.string.resources_not_found);
        }
    }

    //---------------------------------------------------------------------
    // Implements AbsListView.OnScrollListener
    //---------------------------------------------------------------------

    private class ScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = recyclerView.getLayoutManager().getItemCount();
            int firstVisibleItem;

            if (layoutManager instanceof LinearLayoutManager) {
                firstVisibleItem = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
            } else {
                firstVisibleItem = ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
            }

            if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount - mTreshold) {
                loadNextPage();
            }
            enableRefreshLayout(listView);
        }

        private void enableRefreshLayout(RecyclerView listView) {
            boolean enable = !listView.canScrollVertically(-1);
            swipeRefreshLayout.setEnabled(enable);
        }
    }

    //---------------------------------------------------------------------
    // Library selection mode helper
    //---------------------------------------------------------------------

    private class LibrarySelectionModeHelper extends SelectionModeHelper<String> {

        public LibrarySelectionModeHelper(JasperResourceAdapter resourceAdapter) {
            super(((ActionBarActivity) getActivity()), resourceAdapter);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.am_resource_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            menu.findItem(R.id.showAction).setVisible(getSelectedItemCount() == 1);
            MenuItem favoriteMenuItem = menu.findItem(R.id.favoriteAction);

            boolean allItemsAreFavorite = isAllItemsFavorite();
            favoriteMenuItem.setIcon(allItemsAreFavorite ? R.drawable.ic_menu_star : R.drawable.ic_menu_star_outline);
            favoriteMenuItem.setTitle(allItemsAreFavorite ? R.string.r_cm_remove_from_favorites : R.string.r_cm_add_to_favorites);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            ArrayList<String> selectedItemIds = getSelectedItemsKey();
            if (selectedItemIds.size() == 0) return false;

            ResourceLookup selectedItem;
            switch (menuItem.getItemId()) {
                case R.id.showAction:
                    selectedItem = mResourceLookupHashMap.get(selectedItemIds.get(0));
                    String resourceTitle = selectedItem.getLabel();
                    String resourceDescription = selectedItem.getDescription();

                    showInfo(resourceTitle, resourceDescription);
                    return true;
                case R.id.favoriteAction:
                    handleFavoriteMenuAction();
                    invalidateSelectionMode();
                    return true;
                default:
                    return false;
            }
        }

        private boolean isAllItemsFavorite() {
            ArrayList<String> selectedItemIds = getSelectedItemsKey();

            int favoritesCount = 0;

            for (String selectedItem : selectedItemIds) {
                ResourceLookup resource = mResourceLookupHashMap.get(selectedItem);
                if (resource == null) continue;

                Cursor cursor = favoritesHelper.queryFavoriteByResource(resource);

                if (cursor != null) {
                    boolean alreadyFavorite = (cursor.getCount() > 0);
                    if (alreadyFavorite) favoritesCount++;
                    cursor.close();
                }
            }

            return favoritesCount == getSelectedItemCount();
        }

        private void handleFavoriteMenuAction() {
            ArrayList<String> selectedItemIds = getSelectedItemsKey();

            boolean allItemsAreFavorite = isAllItemsFavorite();

            for (String selectedItem : selectedItemIds) {
                ResourceLookup resource = mResourceLookupHashMap.get(selectedItem);
                Uri uri = favoritesHelper.queryFavoriteUri(resource);

                if (allItemsAreFavorite || uri == null) {
                    favoritesHelper.handleFavoriteMenuAction(uri, resource, null);
                }
            }

        }

        private void showInfo(String resourceTitle, String resourceDescription) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            SimpleDialogFragment.createBuilder(getActivity(), fm)
                    .setTitle(resourceTitle)
                    .setMessage(resourceDescription)
                    .setNegativeButtonText(R.string.ok)
                    .show();
        }
    }
}
