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
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.IResourceSearchable;
import com.jaspersoft.android.jaspermobile.activities.repository.support.IResourcesLoader;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.GRID;
import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.LIST;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.switch_menu)
public class ResourcesControllerFragment extends RoboSpiceFragment
        implements IResourcesLoader, IResourceSearchable {
    public static final String TAG = ResourcesControllerFragment.class.getSimpleName();
    public static final String CONTENT_TAG = "CONTENT_TAG";

    @Pref
    RepositoryPref_ repositoryPref;

    @OptionsMenuItem(R.id.switchLayout)
    public MenuItem switchLayoutMenuItem;

    @OptionsMenuItem(R.id.search)
    public MenuItem searchMenuItem;
    @OptionsMenuItem(R.id.showFavorites)
    public MenuItem showFavoritesItem;
    @OptionsMenuItem(R.id.showSettings)
    public MenuItem showSettingsItem;

    @InstanceState
    @FragmentArg
    ArrayList<String> resourceTypes;
    @InstanceState
    @FragmentArg
    boolean recursiveLookup;
    @InstanceState
    @FragmentArg
    String resourceLabel;
    @InstanceState
    @FragmentArg
    String resourceUri;
    @InstanceState
    @FragmentArg
    String query;

    @FragmentArg
    boolean hideMenu;

    private ResourcesFragment contentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ResourcesFragment inMemoryFragment = (ResourcesFragment)
                getFragmentManager().findFragmentByTag(getContentTag());

        if (inMemoryFragment == null) {
            commitContentFragment();
        } else {
            contentFragment = inMemoryFragment;
        }
    }

    private void commitContentFragment() {
        boolean animationEnabled = SettingsActivity.isAnimationEnabled(getActivity());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (animationEnabled) {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        transaction
                .replace(android.R.id.content, getContentFragment(), getContentTag())
                .commit();
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

    private Fragment getContentFragment() {
        contentFragment = ResourcesFragment_.builder()
                .query(query)
                .recursiveLookup(recursiveLookup)
                .resourceUri(resourceUri)
                .resourceLabel(resourceLabel)
                .viewType(getViewType())
                .resourceTypes(resourceTypes).build();
        return contentFragment;
    }

    private ViewType getViewType() {
        return ViewType.valueOf(repositoryPref);
    }

    @Override
    public void loadResourcesByTypes(List<String> types) {
        resourceTypes = Lists.newArrayList(types);
        if (contentFragment != null) {
            contentFragment.loadResourcesByTypes(types);
        }
    }

    public String getContentTag() {
        return TextUtils.isEmpty(resourceUri) ? CONTENT_TAG : CONTENT_TAG + resourceUri;
    }

    @Override
    public void doSearch(String query) {
        contentFragment.setQuery(query);
        contentFragment.loadFirstPage();
    }
}
