/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SearchableActivity_;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.search_menu)
public class SearchControllerFragment extends RoboFragment implements SearchView.OnQueryTextListener {

    public static final String TAG = SearchControllerFragment.class.getSimpleName();
    public static final int SEARCH_ACTION = 100;

    @OptionsMenuItem(R.id.search)
    public MenuItem searchMenuItem;

    @InstanceState
    @FragmentArg
    ArrayList<String> resourceTypes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getString(R.string.s_hint));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchMenuItem.collapseActionView();
        String resourceUri = getResourceUri();
        Intent searchIntent = SearchableActivity_
                .intent(getActivity())
                .query(query)
                .resourceUri(resourceUri)
                .resourceTypes(resourceTypes)
                .controllerTag(getActivity().getLocalClassName())
                .get();
        searchIntent.setAction(Intent.ACTION_SEARCH);
        getActivity().startActivityForResult(searchIntent, SEARCH_ACTION);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = Lists.newArrayList(resourceTypes);
    }

    public String getResourceUri() {
        FragmentManager fm = getFragmentManager();
        int entryCount = fm.getBackStackEntryCount();
        if (entryCount == 0) {
            return ResourcesFragment.ROOT_URI;
        }
        FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(entryCount - 1);
        return entry.getName();
    }
}
