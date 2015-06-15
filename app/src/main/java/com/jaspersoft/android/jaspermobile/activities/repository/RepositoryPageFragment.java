/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositoryControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositoryControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositorySearchFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositorySearchFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class RepositoryPageFragment extends RoboFragment {
    public static final String TAG = RepositoryPageFragment.class.getSimpleName();

    // It is hack to force saved instance state not to be null after rotate
    @InstanceState
    protected boolean initialStart;

    private RepositoryControllerFragment resourcesController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            resourcesController =
                    RepositoryControllerFragment_.builder()
                            .build();
            transaction.replace(R.id.resource_controller, resourcesController, RepositoryControllerFragment.TAG + TAG);

            RepositorySearchFragment searchControllerFragment =
                    RepositorySearchFragment_.builder()
                            .build();
            transaction.replace(R.id.search_controller, searchControllerFragment, RepositorySearchFragment.TAG + TAG);
            transaction.commit();
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            int count = fragmentManager.getBackStackEntryCount();

            if (count == 0) {
                resourcesController = (RepositoryControllerFragment)
                        fragmentManager.findFragmentByTag(RepositoryControllerFragment.TAG + TAG);
            } else {
                FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(count - 1);
                resourcesController = (RepositoryControllerFragment)
                        fragmentManager.findFragmentByTag(RepositoryControllerFragment.TAG + entry.getName());
            }
        }

        ((RoboToolbarActivity) getActivity()).setCustomToolbarView(null);
    }
}
