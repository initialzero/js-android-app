/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.recent.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.SimpleScrollListener;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.filtering.RecentlyViewedResourceFilter;
import com.jaspersoft.android.jaspermobile.util.multichoice.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;

import java.util.List;

import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class RecentFragment extends RoboSpiceFragment
        implements SwipeRefreshLayout.OnRefreshListener, ResourceAdapter.ResourceInteractionListener {
    public static final String TAG = RecentFragment.class.getSimpleName();
    public static final String ROOT_URI = "/";

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

    @FragmentArg
    protected ViewType viewType;

    @Bean
    protected RecentlyViewedResourceFilter recentlyViewedResourceFilter;
    @Bean
    protected ResourceOpener resourceOpener;
    @Bean
    protected FavoritesHelper favoritesHelper;

    private ResourceAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchCriteria.setLimit(10);
        mSearchCriteria.setAccessType("viewed");
        mSearchCriteria.setTypes(recentlyViewedResourceFilter.getCurrent().getValues());
        mSearchCriteria.setSortBy(SortOrder.ACCESS_TIME.getValue());
        mSearchCriteria.setFolderUri(ROOT_URI);
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

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.js_blue,
                R.color.js_dark_blue,
                R.color.js_blue,
                R.color.js_dark_blue);

        listView.setOnScrollListener(new ScrollListener());
        setDataAdapter(savedInstanceState);
        loadResources();
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.recent_card_label));
        }
    }

    @Override
    public void onPause() {
        swipeRefreshLayout.clearAnimation();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAdapter != null) {
            mAdapter.save(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(ResourceLookup resource) {
        mAdapter.finishActionMode();
        resourceOpener.openResource(this, resource);
    }

    //---------------------------------------------------------------------
    // Implements SwipeRefreshLayout.OnRefreshListener
    //---------------------------------------------------------------------

    @Override
    public void onRefresh() {
        loadResources();
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

    private void loadResources() {
        setRefreshState(true);
        showEmptyText(R.string.loading_msg);

        GetResourceLookupsRequest request = new GetResourceLookupsRequest(jsRestClient, mSearchCriteria);
        long cacheExpiryDuration = DurationInMillis.ALWAYS_EXPIRED;
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceLookupsListener());
    }

    private void showEmptyText(int resId) {
        boolean noItems = (mAdapter.getCount() > 0);
        emptyText.setVisibility(noItems ? View.GONE : View.VISIBLE);
        if (resId != 0) emptyText.setText(resId);
    }

    private void setRefreshState(boolean refreshing) {
        if (!refreshing) {
            swipeRefreshLayout.setRefreshing(false);
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
                .setTargetFragment(this)
                .show();
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
            // set data
            List<ResourceLookup> datum = resourceLookupsList.getResourceLookups();
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.addAll(datum);
            mAdapter.setNotifyOnChange(true);
            mAdapter.notifyDataSetChanged();

            // set refresh states
            setRefreshState(false);
            // If need we show 'empty' message
            showEmptyText(R.string.r_browser_nothing_to_display);
        }
    }

    //---------------------------------------------------------------------
    // Implements AbsListView.OnScrollListener
    //---------------------------------------------------------------------

    private class ScrollListener extends SimpleScrollListener {
        @Override
        public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            enableRefreshLayout(listView);
        }

        private void enableRefreshLayout(AbsListView listView) {
            boolean enable = true;
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
