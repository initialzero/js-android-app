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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.support.IResourcesLoader;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.getRepoCacheExpirationValue;

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
    private static final int LOAD_FROM_CACHE = 1;
    private static final int LOAD_FROM_NETWORL = 1;

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

    private int mTotal;
    private ResourceAdapter mAdapter;
    private final DataObservable mObservable = new DataObservable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    // Implements AbsListView.OnItemClickListener
    //---------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ResourceLookup resource = (ResourceLookup) listView.getItemAtPosition(position);
        switch (resource.getResourceType()) {
            case folder:
                openFolder(resource);
                break;
            case reportUnit:
                runReport(resource);
                break;
            case dashboard:
                runDashboard(resource);
                break;
            default:
                break;
        }
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
        mLoaderState = LOAD_FROM_NETWORL;
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
                ? getRepoCacheExpirationValue(getActivity()) : DurationInMillis.ALWAYS_EXPIRED;
        getSpiceManager().execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceLookupsListener());
    }

    private void openFolder(ResourceLookup resource) {
        ResourcesControllerFragment newControllerFragment =
                ResourcesControllerFragment_.builder()
                        .resourceTypes(resourceTypes)
                        .resourceLabel(resource.getLabel())
                        .resourceUri(resource.getUri())
                        .build();
        getFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.controller, newControllerFragment, ResourcesControllerFragment.TAG + resource.getUri())
                .commit();
    }

    private void runReport(final ResourceLookup resource) {
        final GetInputControlsRequest request =
                new GetInputControlsRequest(jsRestClient, resource.getUri());

        ProgressDialogFragment.show(getFragmentManager(),
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (!request.isCancelled()) {
                            getSpiceManager().cancel(request);
                        }
                    }
                },
                new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        getSpiceManager().execute(request,
                                new GetInputControlsListener(resource));
                    }
                });
    }

    private void runDashboard(ResourceLookup resource) {
        Intent htmlViewer = new Intent(getActivity(), DashboardHtmlViewerActivity.class);
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URI, resource.getUri());
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_LABEL, resource.getLabel());
        startActivity(htmlViewer);
    }

    public void showEmptyText(int resId) {
        emptyText.setVisibility((listView.getChildCount() > 0) ? View.GONE : View.VISIBLE);
        emptyText.setText(resId);
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
            showEmptyText(R.string.r_browser_nothing_to_display);

            if (isFirstPage) {
                mTotal = resourceLookupsList.getTotalCount();
            }

            List<ResourceLookup> datum = resourceLookupsList.getResourceLookups();
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

    private class GetInputControlsListener implements RequestListener<InputControlsList> {
        private final ResourceLookup mResource;

        public GetInputControlsListener(ResourceLookup resource) {
            mResource = resource;
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            if (exception instanceof RequestCancelledException) {
                Toast.makeText(getActivity(), R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
            } else {
                RequestExceptionHandler.handle(exception, getActivity(), false);
                ProgressDialogFragment.dismiss(getFragmentManager());
            }
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            ProgressDialogFragment.dismiss(getFragmentManager());

            ArrayList<InputControl> inputControls = new ArrayList<InputControl>(controlsList.getInputControls());

            String reportUri = mResource.getUri();
            String reportLabel = mResource.getLabel();
            if (inputControls.isEmpty()) {
                // Run Report Viewer activity
                Intent htmlViewer = new Intent();
                htmlViewer.setClass(getActivity(), ReportHtmlViewerActivity.class);
                htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URI, reportUri);
                htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_LABEL, reportLabel);
                startActivity(htmlViewer);
            } else {
                // Run Report Options activity
                Intent intent = new Intent(getActivity(), ReportOptionsActivity.class);
                intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI, reportUri);
                intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL, reportLabel);
                intent.putParcelableArrayListExtra(ReportOptionsActivity.EXTRA_REPORT_CONTROLS, inputControls);
                startActivity(intent);
            }
        }
    }

}
