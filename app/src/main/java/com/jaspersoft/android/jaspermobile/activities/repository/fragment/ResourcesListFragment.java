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

import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.fragment_resources_list)
public class ResourcesListFragment extends RoboFragment {
    public static final String TAG = ResourcesListFragment.class.getSimpleName();

    @ViewById(android.R.id.list)
    AbsListView listView;

    @ViewById(android.R.id.empty)
    TextView emptyText;

    @Inject
    JsRestClient jsRestClient;

    @FragmentArg
    ArrayList<String> resourceTypes;

    private ResourceAdapter mAdapter;

    @AfterViews
    final void init() {
        mAdapter = ResourceAdapter.builder(getActivity())
                .setViewType(ViewType.LIST)
                .setJsRestClient(jsRestClient)
                .setTypes(resourceTypes)
                .create();
        mAdapter.setAdapterView(listView);
        mAdapter.loadFirstPage();

        emptyText.setText("List");
        emptyText.setVisibility(View.VISIBLE);
    }
}
