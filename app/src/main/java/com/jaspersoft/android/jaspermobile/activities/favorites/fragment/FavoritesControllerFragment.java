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

package com.jaspersoft.android.jaspermobile.activities.favorites.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.util.ControllerFragment;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FavoritesControllerFragment extends ControllerFragment {
    public static final String TAG = FavoritesControllerFragment.class.getSimpleName();

    private FavoritesFragment contentFragment;

    @FragmentArg
    @InstanceState
    String searchQuery;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FavoritesFragment inMemoryFragment = (FavoritesFragment)
                getFragmentManager().findFragmentByTag(CONTENT_TAG);

        if (inMemoryFragment == null) {
            commitContentFragment();
        } else {
            contentFragment = inMemoryFragment;
        }
    }

    @Override
    public Fragment getContentFragment() {
        contentFragment = FavoritesFragment_.builder()
                .viewType(getViewType())
                .searchQuery(searchQuery)
                .build();
        return contentFragment;
    }

    public void loadItemsByTypes(ResourceLookup.ResourceType newFilterType) {
        if (contentFragment != null) {
            contentFragment.showSavedItemsByFilter(newFilterType);
        }
    }

    public void loadItemsBySortOrder(SortOrder newSortOrder) {
        if (contentFragment != null) {
            contentFragment.showSavedItemsBySortOrder(newSortOrder);
        }
    }

}
