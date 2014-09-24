/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util;

import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterOptions;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.ReportHtmlViewerActivity_;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ResourceOpener {
    @RootContext
    FragmentActivity activity;

    private ArrayList<String> resourceTypes;

    public ResourceOpener() {
        resourceTypes = FilterOptions.ALL_REPOSITORY_TYPES;
    }

    public void setResourceTypes(ArrayList<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public void openResource(ResourceLookup resource) {
        switch (resource.getResourceType()) {
            case folder:
                openFolder(resource);
                break;
            case reportUnit:
                runReport(resource);
                break;
            case dashboard:
                runDashboard(resource);
                break;
            default:
                break;
        }
    }

    private void openFolder(ResourceLookup resource) {
        ResourcesControllerFragment newControllerFragment =
                ResourcesControllerFragment_.builder()
                        .emptyMessage(R.string.r_browser_nothing_to_display)
                        .resourceTypes(resourceTypes)
                        .resourceLabel(resource.getLabel())
                        .resourceUri(resource.getUri())
                        .build();
        activity.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.controller, newControllerFragment, ResourcesControllerFragment.TAG + resource.getUri())
                .commit();
    }

    private void runReport(final ResourceLookup resource) {
        ReportHtmlViewerActivity_.intent(activity)
                .resource(resource).start();
    }

    private void runDashboard(ResourceLookup resource) {
        DashboardHtmlViewerActivity_.intent(activity)
                .resourceLabel(resource.getLabel())
                .resourceUri(resource.getUri()).start();
    }

}
