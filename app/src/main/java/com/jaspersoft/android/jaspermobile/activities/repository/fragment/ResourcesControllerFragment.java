/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;

import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.GRID;
import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.LIST;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.repository_menu)
public class ResourcesControllerFragment extends RoboSpiceFragment {
    public static final String TAG = ResourcesControllerFragment.class.getSimpleName();
    public static final String CONTENT_TAG = "CONTENT_TAG";

    @Pref
    RepositoryPref_ repositoryPref;

    @OptionsMenuItem(R.id.switchLayout)
    public MenuItem switchLayoutMenuItem;

    @FragmentArg
    ArrayList<String> resourceTypes;

    private ResourcesListFragment listFragment;
    private ResourcesGridFragment gridFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getFragmentManager().findFragmentByTag(CONTENT_TAG) == null) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .add(android.R.id.content, getContentFragment(), CONTENT_TAG)
                    .commit();
        }
    }

    @OptionsItem
    final void switchLayout() {
        repositoryPref.viewType()
                .put(getViewType() == LIST ? GRID.toString() : LIST.toString());
        toggleSwitcher();

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(android.R.id.content, getContentFragment(), CONTENT_TAG)
                .commit();

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

    private Fragment getContentFragment() {
        Fragment contentFragment;
        switch (getViewType()) {
            case LIST:
                if (listFragment == null) {
                    listFragment = ResourcesListFragment_.builder()
                            .resourceTypes(resourceTypes).build();
                    }
                    contentFragment = listFragment;
                    break;
                case GRID:
                if (gridFragment == null) {
                    gridFragment = ResourcesGridFragment_.builder()
                            .resourceTypes(resourceTypes).build();
                }
                contentFragment = gridFragment;
                break;
            default:
                throw new UnsupportedOperationException("Unexpected ViewType passed");
        }
        return contentFragment;
    }

    private ViewType getViewType() {
        return ViewType.valueOf(repositoryPref.viewType().get());
    }

}
