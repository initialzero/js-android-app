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

package com.jaspersoft.android.jaspermobile.util;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterManager;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ResourceOpener {
    @Bean
    FilterManager filterManager;
    @RootContext
    FragmentActivity activity;
    @Inject
    JsRestClient jsRestClient;

    private ArrayList<String> resourceTypes;

    @AfterInject
    void injectRoboGuiceDependencies() {
        final RoboInjector injector = RoboGuice.getInjector(activity);
        injector.injectMembersWithoutViews(this);
        resourceTypes = filterManager.getFiltersByType(FilterManager.Type.ALL_FOR_REPOSITORY);
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
                JasperMobileApplication.removeAllCookies();
                runReport(resource);
                break;
            case legacyDashboard:
            case dashboard:
                runDashboard(resource);
                break;
            default:
                break;
        }
    }

    private void openFolder(ResourceLookup resource) {
        // TODO It is smelly fix. Consider this issue while migration to RecyclerView
        ResourcesControllerFragment topController = (ResourcesControllerFragment)
                activity.getSupportFragmentManager().findFragmentByTag(ResourcesControllerFragment.TAG);
        ResourcesControllerFragment_.FragmentBuilder_ builder = ResourcesControllerFragment_.builder()
                .emptyMessage(R.string.r_browser_nothing_to_display)
                .resourceTypes(resourceTypes)
                .resourceLabel(resource.getLabel())
                .resourceUri(resource.getUri());
        if (!TextUtils.isEmpty(topController.controllerTag)) {
            builder.controllerTag(topController.controllerTag);
        }
        ResourcesControllerFragment newControllerFragment = builder.build();

        activity.getSupportFragmentManager().beginTransaction()
                .addToBackStack(resource.getUri())
                .replace(R.id.controller, newControllerFragment, ResourcesControllerFragment.TAG + resource.getUri())
                .commit();
    }

    private void runReport(final ResourceLookup resource) {
        ReportHtmlViewerActivity_.intent(activity)
                .resource(resource).start();
    }

    private void runDashboard(ResourceLookup resource) {
        DashboardHtmlViewerActivity_.intent(activity)
                .resource(resource).start();
    }

}
