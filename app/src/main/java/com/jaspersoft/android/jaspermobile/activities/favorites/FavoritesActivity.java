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

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesSearchFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesSearchFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.LibraryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.robospice.BaseActionBarActivity;
import com.jaspersoft.android.jaspermobile.dialog.FilterFavoritesDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SortDialogFragment;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EActivity(R.layout.repositories_layout)
@OptionsMenu(R.menu.saved_items_menu)
public class FavoritesActivity extends BaseActionBarActivity {

    private FavoritesControllerFragment favoriteController;

    @InstanceState
    ResourceLookup.ResourceType filterType;

    @InstanceState
    SortOrder sortOrder;

    @Pref
    LibraryPref_ pref;

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

            favoriteController = FavoritesControllerFragment_.builder()
                    .build();

            transaction.add(R.id.controller, favoriteController, FavoritesControllerFragment.TAG);

            FavoritesSearchFragment searchFragment = FavoritesSearchFragment_.builder().build();
            transaction.add(searchFragment, FavoritesSearchFragment.TAG);

            transaction.commit();
        }
        else {
            favoriteController = (FavoritesControllerFragment) getSupportFragmentManager()
                    .findFragmentByTag(FavoritesControllerFragment.TAG);
        }
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        super.onBackPressed();
    }

    @OptionsItem(R.id.filter)
    final void startFiltering() {
        FilterFavoritesDialogFragment.show(getSupportFragmentManager(), filterType,
                new FilterFavoritesDialogFragment.FilterFavoritesDialogListener() {
                    @Override
                    public void onDialogPositiveClick(ResourceLookup.ResourceType newFilterType) {
                        if (favoriteController != null) {
                            favoriteController.loadItemsByTypes(newFilterType);
                            filterType = newFilterType;
                        }
                    }
                });
    }

    @OptionsItem(R.id.sort)
    final void startSorting() {
        SortDialogFragment.show(getSupportFragmentManager(), new SortDialogFragment.SortDialogListener() {
            @Override
            public void onOptionSelected(SortOrder _sortOrder) {
                if (favoriteController != null) {
                    favoriteController.loadItemsBySortOrder(_sortOrder);
                    sortOrder = _sortOrder;
                }
            }
        });
    }

}
