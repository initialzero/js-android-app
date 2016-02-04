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

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.info.ResourceInfoActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.Nullable;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.GetRootFoldersCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.resource.SearchResourcesCase;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.filtering.RepositoryResourceFilter;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceAdapter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceConverter;
import com.jaspersoft.android.jaspermobile.util.server.InfoProvider;
import com.jaspersoft.android.jaspermobile.widget.JasperRecyclerView;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import roboguice.inject.InjectView;
import rx.Subscriber;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.fragment_refreshable_resource)
public class RepositoryFragment extends RoboSpiceFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = RepositoryFragment.class.getSimpleName();
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
    protected Analytics analytics;
    @Inject
    @Named("LIMIT")
    protected int mLimit;
    @Inject
    @Named("THRESHOLD")
    protected int mTreshold;

    @Inject
    @Nullable
    protected SearchResourcesCase mSearchResourcesCase;
    @Inject
    @Nullable
    protected GetRootFoldersCase mGetRootFoldersCase;

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

    @InstanceState
    protected boolean mLoading;
    @InstanceState
    protected int mLoaderState = LOAD_FROM_CACHE;
    @InstanceState
    protected boolean mHasNextPage;

    @Bean
    RepositoryResourceFilter repositoryResourceFilter;
    @Bean
    protected DefaultPrefHelper prefHelper;
    @Bean
    protected ResourceOpener resourceOpener;
    @Bean
    protected FavoritesHelper favoritesHelper;
    @Bean
    protected InfoProvider infoProvider;

    private JasperResourceAdapter mAdapter;
    private HashMap<String, ResourceLookup> mResourceLookupHashMap;
    protected ResourceLookupSearchCriteria mSearchCriteria;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GraphObject.Factory.from(getContext())
                .getProfileComponent()
                .inject(this);

        mResourceLookupHashMap = new HashMap<>();

        mSearchCriteria = new ResourceLookupSearchCriteria();
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
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.js_blue,
                R.color.js_dark_blue,
                R.color.js_blue,
                R.color.js_dark_blue);

        listView.addOnScrollListener(new ScrollListener());
        setDataAdapter();
        loadPage();
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean isResourceLoaded = (mAdapter.getItemCount() == 0);
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

    private void clearData() {
        mResourceLookupHashMap.clear();
        mAdapter.clear();
    }

    private void addData(List<ResourceLookup> data) {
        Collections.sort(data, new OrderingByType());
        JasperResourceConverter jasperResourceConverter = new JasperResourceConverter(getActivity());
        mResourceLookupHashMap.putAll(jasperResourceConverter.convertToDataMap(data));
        mAdapter.addAll(jasperResourceConverter.convertToJasperResource(data));
        mAdapter.notifyDataSetChanged();
    }

    private void onViewSingleClick(ResourceLookup resource) {
        resourceOpener.openResource(this, prefTag, resource);
    }

    private void setDataAdapter() {
        JasperResourceConverter jasperResourceConverter = new JasperResourceConverter(getActivity());

        List<ResourceLookup> resourceLookupList = null;
        mAdapter = new JasperResourceAdapter(getActivity(), jasperResourceConverter.convertToJasperResource(resourceLookupList), viewType);
        mAdapter.setOnItemInteractionListener(new JasperResourceAdapter.OnResourceInteractionListener() {
            @Override
            public void onResourceItemClicked(String id) {
                onViewSingleClick(mResourceLookupHashMap.get(id));
            }

            @Override
            public void onSecondaryActionClicked(JasperResource jasperResource) {
                ResourceInfoActivity_.intent(getActivity())
                        .jasperResource(jasperResource)
                        .start();
            }
        });

        listView.setViewType(viewType);
        listView.setAdapter(mAdapter);
    }

    private void loadRootFolders() {
        setRefreshState(true);
        mGetRootFoldersCase.execute(new GetRootFolderDataRequestListener());
    }

    private void loadNextPage() {
        if (!mLoading && mHasNextPage) {
            int currentOffset = mSearchCriteria.getOffset();
            mSearchCriteria.setOffset(currentOffset + mLimit);
            mLoaderState = LOAD_FROM_CACHE;
            loadResources(mLoaderState);
        }
    }


    private void loadResources(int state) {
        setRefreshState(true);
        mSearchResourcesCase.execute(mSearchCriteria, new GetResourceLookupsListener());
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

    private void loadPage() {
        boolean isRoot = TextUtils.isEmpty(resourceUri);
        boolean isProJrs = infoProvider.isProEdition();
        if (isRoot && isProJrs) {
            loadRootFolders();
        } else {
            loadFirstPage();
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private static class OrderingByType implements Comparator<ResourceLookup> {
        @Override
        public int compare(ResourceLookup res1, ResourceLookup res2) {
            int lhs = res1.getResourceType() == ResourceLookup.ResourceType.folder ? 0 : 1;
            int rhs = res2.getResourceType() == ResourceLookup.ResourceType.folder ? 0 : 1;
            return compare(lhs, rhs);
        }

        private static int compare(int lhs, int rhs) {
            return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
        }
    }

    private class GetRootFolderDataRequestListener extends GenericSubscriber<List<FolderDataResponse>> {
        @Override
        public void onNext(List<FolderDataResponse> folderDataResponses) {
            List<ResourceLookup> datum = new ArrayList<>();
            datum.addAll(folderDataResponses);

            addData(datum);

            showEmptyText(R.string.resources_not_found);
        }
    }

    private class GetResourceLookupsListener extends GenericSubscriber<List<ResourceLookup>> {
        @Override
        public void onNext(List<ResourceLookup> resourceLookupsList) {
            mHasNextPage = !resourceLookupsList.isEmpty();
            addData(resourceLookupsList);
            setRefreshState(false);
            showEmptyText(R.string.resources_not_found);
        }
    }

    private abstract class GenericSubscriber<R> extends Subscriber<R> {
        @Override
        public void onStart() {
            showEmptyText(com.jaspersoft.android.jaspermobile.R.string.loading_msg);
        }

        @Override
        public void onCompleted() {
            setRefreshState(false);
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "RepositoryFragment#GetRootFolderDataRequestListener failed");
            RequestExceptionHandler.handle(e, getContext());
            showEmptyText(com.jaspersoft.android.jaspermobile.R.string.failed_load_data);
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

                analytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.LOADED_NEXT.getValue(),  Analytics.EventLabel.REPOSITORY.getValue());
            }
            enableRefreshLayout(listView);
        }

        private void enableRefreshLayout(RecyclerView listView) {
            boolean enable = !listView.canScrollVertically(-1);
            swipeRefreshLayout.setEnabled(enable);
        }
    }
}
