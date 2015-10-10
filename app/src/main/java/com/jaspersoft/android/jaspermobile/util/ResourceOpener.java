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

package com.jaspersoft.android.jaspermobile.util;

import android.accounts.Account;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositoryControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.RepositoryControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.Amber2DashboardActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.AmberDashboardActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.LegacyDashboardViewerActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportViewerActivity_;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.filtering.RepositoryResourceFilter_;
import com.jaspersoft.android.jaspermobile.util.filtering.ResourceFilter;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ResourceOpener {

    @RootContext
    FragmentActivity activity;

    ResourceFilter resourceFilter;
    private ServerRelease serverRelease;
    private boolean isCeJrs;

    @AfterInject
    final void init() {
        Account account = JasperAccountManager.get(activity).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(activity, account);
        serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());
        isCeJrs = accountServerData.getEdition().equals("CE");

        resourceFilter = RepositoryResourceFilter_.getInstance_(activity);
    }

    public void openResource(Fragment fragment, ResourceLookup resource) {
        openResource(fragment, RepositoryControllerFragment.PREF_TAG, resource);
    }

    public void openResource(Fragment fragment, String prefTag, ResourceLookup resource) {
        switch (resource.getResourceType()) {
            case folder:
                openFolder(fragment, prefTag, resource);
                break;
            case reportUnit:
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

    public void openFolder(Fragment fragment, String preftag, ResourceLookup resource) {
        RepositoryControllerFragment newControllerFragment =
                RepositoryControllerFragment_.builder()
                        .resourceLabel(resource.getLabel())
                        .resourceUri(resource.getUri())
                        .prefTag(preftag)
                        .build();
        fragment.getFragmentManager().beginTransaction()
                .addToBackStack(resource.getUri())
                .replace(R.id.resource_controller, newControllerFragment)
                .commit();
    }

    private void runReport(final ResourceLookup resource) {
        if (isCeJrs || serverRelease.code() < ServerRelease.AMBER.code()) {
            ReportHtmlViewerActivity_.intent(activity)
                    .resource(resource).start();
        } else {
            ReportViewerActivity_.intent(activity)
                    .resource(resource).start();
        }
    }

    private void runDashboard(ResourceLookup resource) {
        if (resource.getResourceType() == ResourceLookup.ResourceType.legacyDashboard || serverRelease.code() < ServerRelease.AMBER.code()) {
            LegacyDashboardViewerActivity_.intent(activity).resource(resource).start();
        } else if (serverRelease.code() == ServerRelease.AMBER_MR1.code()) {
            AmberDashboardActivity_.intent(activity).resource(resource).start();
        } else {
            Amber2DashboardActivity_.intent(activity).resource(resource).start();
        }
    }
}
