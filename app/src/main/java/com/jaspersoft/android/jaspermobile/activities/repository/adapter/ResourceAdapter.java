/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceAdapter extends ArrayAdapter<ResourceLookup>
        implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private static final int LIMIT = 40;
    private static final int THRESHOLD = 5;

    private final ResourceViewHelper viewHelper = new ResourceViewHelper();

    private final ViewType mViewType;
    private final ResourceLookupSearchCriteria mSearchCriteria;
    private final JsRestClient mJsRestClient;

    private AbsListView adapterView;
    private View pendingView;
    private SpiceManager mSpiceManager;

    private int mTotal;
    private boolean mLoading;
    private boolean keepOnAppending;

    private ResourceAdapter(Context context, JsRestClient restClient, ViewType viewType, ArrayList<String> types) {
        super(context, 0);
        checkNotNull(types, "Type can`t be null");
        mJsRestClient = checkNotNull(restClient, "Rest client can`t be null");
        mViewType = checkNotNull(viewType, "ViewType can`t be null");
        mSearchCriteria = new ResourceLookupSearchCriteria();
        mSearchCriteria.setTypes(types);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (keepOnAppending && position == super.getCount()) {
            return getPendingView(parent);
        } else {
            return getViewImpl(position, (IResourceView) convertView);
        }
    }

    private View getPendingView(ViewGroup parent) {
        if (pendingView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            pendingView = inflater.inflate(R.layout.list_indeterminate_progress, parent, false);
        }
        return pendingView;
    }

    private View getViewImpl(int position, IResourceView convertView) {
        IResourceView itemView;
        if (mViewType == ViewType.LIST) {
            itemView = convertView;
        } else {
            itemView = convertView;
        }

        if (itemView == null) {
            if (mViewType == ViewType.LIST) {
                itemView = ListItemView_.build(getContext());
            } else {
                itemView = GridItemView_.build(getContext());
            }
        }

        viewHelper.populateView(itemView, getItem(position));

        return (View) itemView;
    }

    public void setAdapterView(AbsListView listView) {
        adapterView = checkNotNull(listView, "Trying to set empty AdapterView");
        adapterView.setOnItemClickListener(this);
        adapterView.setOnScrollListener(this);
    }

    public void setSpiceManager(SpiceManager spiceManager) {
        mSpiceManager = checkNotNull(spiceManager);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ResourceLookup resourceLookup = getItem(position);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void loadFirstPage() {
        mSearchCriteria.setOffset(0);
        mSearchCriteria.setLimit(LIMIT);
        loadResources();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount - THRESHOLD) {
            loadNextPage();
        }
    }

    private void loadNextPage() {
        if (!mLoading && hasNextPage()) {
            mSearchCriteria.setOffset(mSearchCriteria.getOffset() + LIMIT);
            loadResources();
        }
    }

    private boolean hasNextPage() {
        return mSearchCriteria.getOffset() + LIMIT < mTotal;
    }

    private void loadResources() {
        mLoading = true;
        GetResourceLookupsRequest request = new GetResourceLookupsRequest(mJsRestClient, mSearchCriteria);
        long cacheExpiryDuration = SettingsActivity.getRepoCacheExpirationValue(getContext());
        mSpiceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, new GetResourceLookupsListener());
    }

    private class GetResourceLookupsListener implements RequestListener<ResourceLookupsList> {
        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, (android.app.Activity) getContext(), true);
        }

        @Override
        public void onRequestSuccess(ResourceLookupsList resourceLookupsList) {
            mLoading = false;
            boolean isFirstPage = mSearchCriteria.getOffset() == 0;

            if (isFirstPage) {
                mTotal = resourceLookupsList.getTotalCount();
            }

            keepOnAppending = true;
            addAll(resourceLookupsList.getResourceLookups());
        }
    }

    public static Builder builder(Context context) {
        checkNotNull(context);
        return new Builder(context);
    }

    public static class Builder {
        private final Context context;

        private ViewType viewType;
        private JsRestClient jsRestClient;
        private ArrayList<String> types;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setViewType(ViewType viewType) {
            this.viewType = viewType;
            return this;
        }

        public Builder setTypes(ArrayList<String> types) {
            this.types = types;
            return this;
        }

        public Builder setJsRestClient(JsRestClient jsRestClient) {
            this.jsRestClient = jsRestClient;
            return this;
        }

        public ResourceAdapter create() {
            return new ResourceAdapter(context, jsRestClient, viewType, types);
        }
    }
}
