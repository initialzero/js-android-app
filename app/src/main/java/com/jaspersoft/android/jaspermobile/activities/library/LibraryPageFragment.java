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
package com.jaspersoft.android.jaspermobile.activities.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.library.fragment.LibraryControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.library.fragment.LibraryControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.library.fragment.LibrarySearchFragment;
import com.jaspersoft.android.jaspermobile.activities.library.fragment.LibrarySearchFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.dialog.SortDialogFragment;
import com.jaspersoft.android.jaspermobile.util.filtering.Filter;
import com.jaspersoft.android.jaspermobile.util.filtering.LibraryResourceFilter;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOptions;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;
import com.jaspersoft.android.jaspermobile.widget.FilterTitleView;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.sharedpreferences.Pref;

import roboguice.fragment.RoboFragment;


/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu(R.menu.sort_menu)
@EFragment (R.layout.content_layout)
public class LibraryPageFragment extends RoboFragment implements SortDialogFragment.SortDialogClickListener {

    @Inject
    protected JsRestClient jsRestClient;

    @Pref
    protected LibraryPref_ pref;
    @Bean
    protected LibraryResourceFilter libraryResourceFilter;
    @Bean
    protected SortOptions sortOptions;

    private LibraryControllerFragment libraryControllerFragment;

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
            pref.sortType().put(null);

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            libraryControllerFragment = LibraryControllerFragment_.builder()
                    .sortOrder(sortOptions.getOrder())
                    .build();
            transaction.replace(R.id.resource_controller, libraryControllerFragment, LibraryControllerFragment.TAG);

            LibrarySearchFragment searchControllerFragment = LibrarySearchFragment_.builder()
                    .build();
            transaction.replace(R.id.search_controller, searchControllerFragment);
            transaction.commit();
        } else {
            libraryControllerFragment = (LibraryControllerFragment) getFragmentManager()
                    .findFragmentByTag(LibraryControllerFragment.TAG);
        }

        FilterTitleView filterTitleView = new FilterTitleView(getActivity());
        boolean filterViewInitialized = filterTitleView.init(libraryResourceFilter);
        if (filterViewInitialized) {
            filterTitleView.setFilterSelectedListener(new FilterChangeListener());
            ((RoboToolbarActivity) getActivity()).setDisplayCustomToolbarEnable(true);
            ((RoboToolbarActivity) getActivity()).setCustomToolbarView(filterTitleView);
        } else {
            ((RoboToolbarActivity) getActivity()).setCustomToolbarView(null);
        }
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
        sortOptions.putOrder(sortOrder);

        if (libraryControllerFragment != null) {
            libraryControllerFragment.loadResourcesBySortOrder(sortOrder);
        }
    }

    private class FilterChangeListener implements FilterTitleView.FilterListener {
        @Override
        public void onFilter(Filter filter) {
            libraryResourceFilter.persist(filter);
            if (libraryControllerFragment != null) {
                libraryControllerFragment.loadResourcesByTypes();
            }
        }
    }
}
