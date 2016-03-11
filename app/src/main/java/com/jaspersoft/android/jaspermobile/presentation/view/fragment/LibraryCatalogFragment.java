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

package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.info.ResourceInfoActivity_;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.FragmentModule;
import com.jaspersoft.android.jaspermobile.presentation.contract.LibraryContract;
import com.jaspersoft.android.jaspermobile.presentation.presenter.LibraryPresenter;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceAdapter;
import com.jaspersoft.android.jaspermobile.widget.JasperRecyclerView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class LibraryCatalogFragment extends BaseFragment implements LibraryContract.View, SwipeRefreshLayout.OnRefreshListener {

    private JasperRecyclerView resourcesList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView message;

    @Inject
    LibraryPresenter libraryPresenter;
    @Inject
    @Named("THRESHOLD")
    protected int mTreshold;

    private JasperResourceAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProfileComponent()
                .plus(new FragmentModule(this))
                .inject(this);

        libraryPresenter.injectView(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_refreshable_resource, container, false);
        resourcesList = (JasperRecyclerView) fragment.findViewById(android.R.id.list);
        swipeRefreshLayout = (SwipeRefreshLayout) fragment.findViewById(R.id.refreshLayout);
        message = (TextView) fragment.findViewById(android.R.id.empty);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.js_blue,
                R.color.js_dark_blue,
                R.color.js_blue,
                R.color.js_dark_blue);

        setDataAdapter();

        libraryPresenter.onReady();

        return fragment;
    }

    @Override
    public void onRefresh() {
        libraryPresenter.onRefresh();
    }

    @Override
    public void showResources(List<JasperResource> jasperResourceList) {
        mAdapter.setResources(jasperResourceList);
    }

    @Override
    public void clearResources() {
        mAdapter.clear();
    }

    @Override
    public void showFirstLoading() {
        message.setVisibility(View.VISIBLE);
        message.setText(R.string.loading_msg);
    }

    @Override
    public void showNextLoading() {
        mAdapter.showLoading();
    }

    @Override
    public void hideLoading() {
        swipeRefreshLayout.setRefreshing(false);
        message.setVisibility(View.GONE);

        mAdapter.hideLoading();
    }

    private void setDataAdapter() {
        mAdapter = new JasperResourceAdapter(getActivity(), new ArrayList<JasperResource>());
        resourcesList.setAdapter(mAdapter);
        resourcesList.addOnScrollListener(new ScrollListener());
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
                libraryPresenter.onScrollToEnd();
            }
        }
    }
}
