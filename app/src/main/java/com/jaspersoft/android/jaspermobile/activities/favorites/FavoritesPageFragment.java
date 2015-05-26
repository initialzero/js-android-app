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

package com.jaspersoft.android.jaspermobile.activities.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesSearchFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesSearchFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.LibraryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOptions;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.dialog.FilterFavoritesDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SortDialogFragment;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.sharedpreferences.Pref;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.saved_items_menu)
public class FavoritesPageFragment extends RoboFragment implements SortDialogFragment.SortDialogClickListener, FragmentManager.OnBackStackChangedListener {
    public static final String TAG = FavoritesPageFragment.class.getSimpleName();

    private FavoritesControllerFragment favoriteController;

    @InstanceState
    protected ResourceLookup.ResourceType filterType;

    @Bean
    protected SortOptions sortOptions;

    @Pref
    protected LibraryPref_ pref;

    @OptionsMenuItem(R.id.filter)
    protected MenuItem filterMenuItem;
    @OptionsMenuItem(R.id.sort)
    protected MenuItem sortMenuItem;

    private boolean mShowFilters;

    private final FilterFavoritesDialogFragment.FilterFavoritesDialogListener mFilterSelectedListener =
            new FilterFavoritesDialogFragment.FilterFavoritesDialogListener() {
                @Override
                public void onDialogPositiveClick(ResourceLookup.ResourceType newFilterType) {
                    if (favoriteController != null) {
                        favoriteController.loadItemsByTypes(newFilterType);
                        filterType = newFilterType;
                    }
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        calculateFilterCondition();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        calculateFilterCondition();
        filterMenuItem.setVisible(mShowFilters);
        sortMenuItem.setVisible(mShowFilters);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            // Reset all controls state
            pref.clear();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            favoriteController = FavoritesControllerFragment_.builder()
                    .filterType(filterType)
                    .sortOrder(sortOptions.getOrder())
                    .build();

            transaction.replace(R.id.resource_controller, favoriteController, FavoritesControllerFragment.TAG);

            FavoritesSearchFragment searchFragment = FavoritesSearchFragment_.builder().build();
            transaction.replace(R.id.search_controller, searchFragment, FavoritesSearchFragment.TAG);

            transaction.commit();
        } else {
            favoriteController = (FavoritesControllerFragment) getFragmentManager()
                    .findFragmentByTag(FavoritesControllerFragment.TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getFragmentManager().addOnBackStackChangedListener(this);

        FilterFavoritesDialogFragment.attachListener(getFragmentManager(), filterType, mFilterSelectedListener);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.f_title);
        }
    }

    @Override
    public void onPause() {
        getFragmentManager().removeOnBackStackChangedListener(this);
        super.onPause();
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        getActivity().onBackPressed();
    }

    @OptionsItem(R.id.filter)
    final void startFiltering() {
        FilterFavoritesDialogFragment.show(getFragmentManager(), filterType, mFilterSelectedListener);
    }

    @OptionsItem(R.id.sort)
    final void startSorting() {
        SortDialogFragment.createBuilder(getFragmentManager())
                .setInitialSortOption(sortOptions.getOrder())
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onOptionSelected(SortOrder sortOrder) {
        if (favoriteController != null) {
            favoriteController.loadItemsBySortOrder(sortOrder);
            sortOptions.putOrder(sortOrder);
        }
    }

    @Override
    public void onBackStackChanged() {
        getActivity().supportInvalidateOptionsMenu();
    }

    private void calculateFilterCondition() {
        mShowFilters = (getFragmentManager().getBackStackEntryCount() == 0);
    }
}
