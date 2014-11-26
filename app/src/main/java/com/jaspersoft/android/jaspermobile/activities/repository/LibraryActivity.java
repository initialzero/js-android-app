/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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
package com.jaspersoft.android.jaspermobile.activities.repository;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.SearchControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.SearchControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterOptions;
import com.jaspersoft.android.jaspermobile.activities.repository.support.LibraryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOptions;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.dialog.FilterDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SortDialogFragment;
import com.jaspersoft.android.jaspermobile.info.ServerInfoManager;
import com.jaspersoft.android.jaspermobile.info.ServerInfoSnapshot;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EActivity
@OptionsMenu(R.menu.libraries_menu)
public class LibraryActivity extends RoboSpiceFragmentActivity {

    @Inject
    JsRestClient jsRestClient;
    @Inject
    ServerInfoManager infoManager;

    @Pref
    LibraryPref_ pref;
    @Bean
    FilterOptions filterOptions;
    @Bean
    SortOptions sortOptions;

    @OptionsMenuItem
    MenuItem filter;
    @OptionsMenuItem
    MenuItem sort;

    @InstanceState
    boolean mShowFilterOption;
    @InstanceState
    boolean mShowSortOption;

    private ResourcesControllerFragment resourcesController;
    private SearchControllerFragment searchControllerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Reset all controls state
            pref.clear();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            resourcesController =
                    ResourcesControllerFragment_.builder()
                            .emptyMessage(R.string.r_browser_nothing_to_display)
                            .resourceTypes(filterOptions.getFilters())
                            .sortOrder(sortOptions.getOrder())
                            .recursiveLookup(true)
                            .build();
            transaction.add(resourcesController, ResourcesControllerFragment.TAG);

            searchControllerFragment =
                    SearchControllerFragment_.builder()
                            .resourceTypes(filterOptions.getFilters())
                            .build();
            transaction.add(searchControllerFragment, SearchControllerFragment.TAG);
            transaction.commit();
        } else {
            resourcesController = (ResourcesControllerFragment) getSupportFragmentManager()
                    .findFragmentByTag(ResourcesControllerFragment.TAG);
            searchControllerFragment = (SearchControllerFragment) getSupportFragmentManager()
                    .findFragmentByTag(SearchControllerFragment.TAG);
        }

        updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        infoManager.getServerInfo(getSpiceManager(), new ServerInfoManager.InfoCallback() {
            @Override
            public void onInfoReceived(ServerInfoSnapshot serverInfo) {
                mShowSortOption = true;
                String proVersion = ServerInfo.EDITIONS.PRO;
                mShowFilterOption = (proVersion.equals(serverInfo.getEdition()));
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        filter.setVisible(mShowFilterOption);
        sort.setVisible(mShowSortOption);
        return true;
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        HomeActivity.goHome(this);
    }

    @OptionsItem(R.id.filter)
    final void startFiltering() {
        FilterDialogFragment.show(getSupportFragmentManager(),
                new FilterDialogFragment.FilterDialogListener() {
                    @Override
                    public void onDialogPositiveClick(List<String> types) {
                        if (resourcesController != null) {
                            resourcesController.loadResourcesByTypes(types);
                        }
                        if (searchControllerFragment != null) {
                            searchControllerFragment.setResourceTypes(types);
                        }
                    }
                });
    }

    @OptionsItem(R.id.sort)
    final void startSorting() {
        SortDialogFragment.show(getSupportFragmentManager(), new SortDialogFragment.SortDialogListener() {
            @Override
            public void onOptionSelected(SortOrder sortOrder) {
                if (resourcesController != null) {
                    resourcesController.loadResourcesBySortOrder(sortOrder);
                }
            }
        });
    }

}
