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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositorySearchableActivity_;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.search_menu)
public class RepositorySearchFragment extends Fragment implements SearchView.OnQueryTextListener {

    @OptionsMenuItem(R.id.search)
    public MenuItem searchMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        if (isAdded()) {
            disableSearchViewActionMode(searchView);
            searchView.setQueryHint(getString(R.string.s_hint));
            searchView.setOnQueryTextListener(this);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchMenuItem.collapseActionView();
        String resourceUri = getResourceUri();
        Intent searchIntent = RepositorySearchableActivity_
                .intent(getActivity())
                .query(query)
                .resourceUri(resourceUri)
                .get();
        getActivity().startActivity(searchIntent);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public String getResourceUri() {
        FragmentManager fm = getFragmentManager();
        int entryCount = fm.getBackStackEntryCount();
        if (entryCount == 0) {
            return RepositoryFragment.ROOT_URI;
        }
        FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(entryCount - 1);
        return entry.getName();
    }

    private void disableSearchViewActionMode(SearchView searchView) {
        EditText searchInput = (EditText) searchView.findViewById(R.id.search_src_text);
        if (searchInput != null) {
            searchInput.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
    }
}
