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

import android.database.DataSetObserver;
import android.util.Log;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesFragment;
import com.jaspersoft.android.jaspermobile.test.utils.espresso.ActivityLifecycleIdlingResource;

import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ResourcesFragmentIdlingResource implements ActivityLifecycleIdlingResource<ResourcesFragment> {
    private ResourceCallback callback;
    private ResourcesFragment resourcesFragment;
    private ListView listView;

    @Override
    public void inject(ResourcesFragment activityComponent) {
        this.resourcesFragment = checkNotNull(activityComponent,
                String.format("Trying to instantiate a \'%s\' with a null ResourcesFragment", getName()));
        listView = (ListView) resourcesFragment.getView();
        listView.getAdapter().registerDataSetObserver(new DataObservable());
    }

    @Override
    public void clear() {
        resourcesFragment = null;
    }

    @Override
    public String getName() {
        return "ResourcesFragment adapter idling resource";
    }

    @Override
    public boolean isIdleNow() {
        boolean isIdling = true;
        if (resourcesFragment == null) {
            isIdling = true;
            Log.d("isIdleNow", "(resourcesFragment == null) ============> " + isIdling);
            return true;
        }
        isIdling = (!resourcesFragment.isLoading() && callback != null);
        Log.d("isIdleNow", "(!resourcesFragment.isLoading() && callback != null) ============> " + isIdling);
        return isIdling;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }

    private class DataObservable extends DataSetObserver {
        public void onChanged() {
            super.onChanged();
            callback.onTransitionToIdle();
        }
    }
}
