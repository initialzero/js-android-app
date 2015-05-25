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

package com.jaspersoft.android.jaspermobile.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ControllerPref;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;

import roboguice.fragment.RoboFragment;

import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.GRID;
import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.LIST;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class ControllerFragment extends RoboFragment {
    protected final static String PREF_TAG_KEY = "prefTag";

    private MenuItem switchLayoutMenuItem;
    protected ControllerPref controllerPref;
    private int switchMenuIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.switch_menu, menu);
        switchLayoutMenuItem = menu.findItem(R.id.switchLayout);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        toggleSwitcher();
    }

    @Override
    public void onStart() {
        super.onStart();
        initControllerPref();
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean switchButtonInitialized = switchMenuIcon != 0;
        boolean switchViewWasAltered = switchMenuIcon != getToggleIcon();

        if (switchButtonInitialized && switchViewWasAltered) {
            switchLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.switchLayout) {
            togglePref();
            switchLayout();
            return true;
        }
        return false;
    }

    private void togglePref() {
        controllerPref.viewType()
                .put(getViewType() == LIST ? GRID.toString() : LIST.toString());
    }

    private void switchLayout() {
        toggleSwitcher();
        commitContentFragment();
        getActivity().supportInvalidateOptionsMenu();
    }

    private void toggleSwitcher() {
        switchMenuIcon = getToggleIcon();
        if (switchLayoutMenuItem != null) {
            switchLayoutMenuItem.setIcon(switchMenuIcon);
        }
    }

    private int getToggleIcon() {
        return getViewType() == LIST ? R.drawable.ic_menu_module : R.drawable.ic_menu_list;
    }

    protected void commitContentFragment() {
        boolean animationEnabled = DefaultPrefHelper_.getInstance_(getActivity()).isAnimationEnabled();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (animationEnabled) {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        transaction
                .replace(R.id.content_frame, getContentFragment(), getContentFragmentTag())
                .commit();
    }

    protected ViewType getViewType() {
        if (controllerPref == null) {
            initControllerPref();
        }
        return ViewType.valueOf(controllerPref);
    }


    private void initControllerPref() {
        String prefTag = getArguments().getString(PREF_TAG_KEY);
        controllerPref = new ControllerPref(getActivity(), prefTag);
    }

    protected abstract Fragment getContentFragment();

    protected abstract String getContentFragmentTag();

}
