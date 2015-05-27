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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesSearchFragment;
import com.jaspersoft.android.jaspermobile.activities.favorites.fragment.FavoritesSearchFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.LibraryPref_;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.sharedpreferences.Pref;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FavoritesPageFragment extends RoboFragment {
    public static final String TAG = FavoritesPageFragment.class.getSimpleName();

    // It is hack to force saved instance state not to be null after rotate
    @InstanceState
    protected boolean initialStart;

    @Pref
    protected LibraryPref_ pref;
    private FavoritesControllerFragment favoriteController;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            // Reset all controls state
            pref.clear();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            favoriteController = FavoritesControllerFragment_.builder()
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
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.f_title);
        }
    }
}
