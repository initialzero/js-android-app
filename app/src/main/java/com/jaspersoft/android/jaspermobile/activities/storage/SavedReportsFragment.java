/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.storage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.SearchControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.support.LibraryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOptions;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsSearchFragment;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsSearchFragment_;
import com.jaspersoft.android.jaspermobile.dialog.FilterSavedItemsDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SortDialogFragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.sharedpreferences.Pref;

import roboguice.fragment.RoboFragment;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.8
 */
@EFragment
@OptionsMenu(R.menu.saved_items_menu)
public class SavedReportsFragment extends RoboFragment implements SortDialogFragment.SortDialogClickListener{
    public static final String TAG = SavedReportsFragment.class.getSimpleName();

    private SavedItemsControllerFragment savedItemsController;

    @InstanceState
    FileAdapter.FileType filterType;

    @Bean
    protected SortOptions sortOptions;

    @Pref
    LibraryPref_ pref;


    private final FilterSavedItemsDialogFragment.FilterSavedItemsDialogListener mFilterSelectedListener =
            new FilterSavedItemsDialogFragment.FilterSavedItemsDialogListener() {
        @Override
        public void onDialogPositiveClick(FileAdapter.FileType _filterType) {
            if (savedItemsController != null) {
                savedItemsController.loadItemsByTypes(_filterType);
                filterType = _filterType;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            // Reset all controls state
            pref.clear();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            savedItemsController = SavedItemsControllerFragment_.builder()
                    .filterType(filterType)
                    .sortOrder(sortOptions.getOrder())
                    .build();
            transaction.replace(R.id.resource_controller, savedItemsController, SavedItemsControllerFragment.TAG);

            SavedItemsSearchFragment searchFragment = SavedItemsSearchFragment_.builder().build();
            transaction.replace(R.id.search_controller, searchFragment, SearchControllerFragment.TAG + TAG);

            transaction.commit();
        } else {
            savedItemsController = (SavedItemsControllerFragment) getFragmentManager()
                    .findFragmentByTag(SavedItemsControllerFragment.TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FilterSavedItemsDialogFragment.attachListener(getFragmentManager(), filterType, mFilterSelectedListener);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sdr_ab_title);
        }
    }

    @OptionsItem(android.R.id.home)
    final void goHome() {
        getActivity().onBackPressed();
    }

    @OptionsItem(R.id.filter)
    final void startFiltering() {
        FilterSavedItemsDialogFragment.show(getFragmentManager(), filterType, mFilterSelectedListener);
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
        if (savedItemsController != null) {
            savedItemsController.loadItemsBySortOrder(sortOrder);
            sortOptions.putOrder(sortOrder);
        }
    }
}
