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

package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.ControllerFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class RepositoryControllerFragment extends ControllerFragment {
    public static final String TAG = RepositoryControllerFragment.class.getSimpleName();
    public static final String PREF_TAG = "repository_pref";
    public static final String CONTENT_TAG = "ResourcesControllerFragment.CONTENT_TAG";

    @InstanceState
    @FragmentArg
    String prefTag;
    @InstanceState
    @FragmentArg
    String resourceUri;
    @InstanceState
    @FragmentArg
    String query;
    @InstanceState
    @FragmentArg
    String resourceLabel;

    @FragmentArg
    boolean hideMenu;

    private RepositoryFragment contentFragment;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RepositoryFragment inMemoryFragment = (RepositoryFragment)
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
        ((RoboToolbarActivity) getActivity()).setCustomToolbarView(null);
        getArguments().putString(PREF_TAG_KEY, prefTag != null ? prefTag : PREF_TAG);
    }

    @Override
    public Fragment getContentFragment() {
        contentFragment = RepositoryFragment_.builder()
                .query(query)
                .resourceUri(resourceUri)
                .resourceLabel(resourceLabel)
                .viewType(getViewType())
                .build();
        return contentFragment;
    }

    @Override
    protected String getContentFragmentTag() {
        return TextUtils.isEmpty(resourceUri) ? CONTENT_TAG : CONTENT_TAG + resourceUri;
    }
}
