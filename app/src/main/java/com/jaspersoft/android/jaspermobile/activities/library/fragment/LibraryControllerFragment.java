/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.library.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.jaspersoft.android.jaspermobile.util.ControllerFragment;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.util.ArrayList;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class LibraryControllerFragment extends ControllerFragment {
    public static final String TAG = LibraryControllerFragment.class.getSimpleName();
    public static final String PREF_TAG = "library_pref";

    @InstanceState
    @FragmentArg
    SortOrder sortOrder;
    @InstanceState
    @FragmentArg
    String query;
    @InstanceState
    @FragmentArg
    String resourceLabel;

    private LibraryFragment contentFragment;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LibraryFragment inMemoryFragment = (LibraryFragment)
                getFragmentManager().findFragmentByTag(getContentFragmentTag());

        if (inMemoryFragment == null) {
            commitContentFragment();
        } else {
            contentFragment = inMemoryFragment;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArguments().putString(PREF_TAG_KEY, PREF_TAG);
    }

    @Override
    public Fragment getContentFragment() {
        contentFragment = LibraryFragment_.builder()
                .query(query)
                .viewType(getViewType())
                .sortOrder(sortOrder)
                .resourceLabel(resourceLabel)
                .build();
        return contentFragment;
    }

    @Override
    protected String getContentFragmentTag() {
        return LibraryFragment.TAG;
    }

    public void loadResourcesByTypes() {
        if (contentFragment != null) {
            contentFragment.loadResourcesByTypes();
        }
    }

    public void loadResourcesBySortOrder(SortOrder order) {
        sortOrder = order;
        if (contentFragment != null) {
            contentFragment.loadResourcesBySortOrder(order);
        }
    }

    public void handleVoiceCommand(ArrayList<String> matches) {
       if (contentFragment != null) {
           contentFragment.handleVoiceCommand(matches);
       }
    }
}
