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

package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import android.app.Activity;
import android.content.ComponentName;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleCallback;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ResourceFragmentInjector implements ActivityLifecycleCallback {
    private final ResourcesFragmentIdlingResource adapterIdlingResource;

    public ResourceFragmentInjector(ResourcesFragmentIdlingResource adapterIdlingResource) {
        this.adapterIdlingResource = adapterIdlingResource;
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        ComponentName targetComponentName =
                new ComponentName(activity, LibraryActivity_.class.getName());

        ComponentName currentComponentName = activity.getComponentName();
        if (!currentComponentName.equals(targetComponentName)) return;

        switch (stage) {
            case STARTED:
                LibraryActivity libraryActivity = (LibraryActivity) activity;
                ResourcesFragment resourcesFragment =
                        (ResourcesFragment) libraryActivity.getSupportFragmentManager()
                                .findFragmentByTag(ResourcesControllerFragment.CONTENT_TAG);
                adapterIdlingResource.inject(resourcesFragment);
                break;
            case STOPPED:
                // Clean up reference
                if (activity.isFinishing()) adapterIdlingResource.clear();
                break;
            default: // NOP
        }
    }

}
