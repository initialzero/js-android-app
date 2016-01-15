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

package com.jaspersoft.android.jaspermobile.activities.repository;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
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
@EFragment (R.layout.content_layout)
public class RepositoryPageFragment extends RoboFragment {

    // It is hack to force saved instance state not to be null after rotate
    @InstanceState
    protected boolean initialStart;
    @Inject
    protected Analytics analytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            RepositoryControllerFragment resourcesController = RepositoryControllerFragment_.builder()
                    .build();
            transaction.replace(R.id.resource_controller, resourcesController);

            RepositorySearchFragment searchControllerFragment =
                    RepositorySearchFragment_.builder()
                            .build();
            transaction.replace(R.id.search_controller, searchControllerFragment);
            transaction.commit();

            analytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.VIEWED.getValue(), Analytics.EventLabel.REPOSITORY.getValue());
        }

        ((RoboToolbarActivity) getActivity()).setCustomToolbarView(null);
    }
}
