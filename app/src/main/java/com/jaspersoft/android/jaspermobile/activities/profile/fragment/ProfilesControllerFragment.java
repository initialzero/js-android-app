/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.profile.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.sharedpreferences.Pref;

import roboguice.fragment.RoboFragment;

import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.GRID;
import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.LIST;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.switch_menu)
public class ProfilesControllerFragment extends RoboFragment {

    public static final String TAG = ProfilesControllerFragment.class.getSimpleName();
    public static final String CONTENT_TAG = "CONTENT_TAG";

    @OptionsMenuItem(R.id.switchLayout)
    public MenuItem switchLayoutMenuItem;
    @Pref
    RepositoryPref_ repositoryPref;

    private ServersFragment contentFragment;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ServersFragment inMemoryFragment = (ServersFragment)
                getFragmentManager().findFragmentByTag(CONTENT_TAG);

        if (inMemoryFragment == null) {
            commitContentFragment();
        } else {
            contentFragment = inMemoryFragment;
        }
    }

    @OptionsItem
    final void switchLayout() {
        repositoryPref.viewType()
                .put(getViewType() == LIST ? GRID.toString() : LIST.toString());
        toggleSwitcher();
        commitContentFragment();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        toggleSwitcher();
    }

    private void toggleSwitcher() {
        if (getViewType() == LIST) {
            switchLayoutMenuItem.setIcon(R.drawable.ic_collections_view_as_grid);
        } else {
            switchLayoutMenuItem.setIcon(R.drawable.ic_collections_view_as_list);
        }
    }

    private void commitContentFragment() {
        boolean animationEnabled = SettingsActivity.isAnimationEnabled(getActivity());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (animationEnabled) {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        transaction
                .replace(android.R.id.content, getContentFragment(), CONTENT_TAG)
                .commit();
    }

    private Fragment getContentFragment() {
        contentFragment = ServersFragment_.builder()
                .viewType(getViewType()).build();
        return contentFragment;
    }

    private ViewType getViewType() {
        return ViewType.valueOf(repositoryPref);
    }

}
