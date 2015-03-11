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

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterManager;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.CordovaDashboardActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.DashboardViewerActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportViewerActivity_;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

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

    private ArrayList<String> resourceTypes;

    public void setResourceTypes(ArrayList<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public void openResource(Fragment fragment, ResourceLookup resource) {
        switch (resource.getResourceType()) {
            case folder:
                openFolder(fragment, resource);
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

    private void openFolder(Fragment fragment, ResourceLookup resource) {
        ResourcesControllerFragment newControllerFragment =
                ResourcesControllerFragment_.builder()
                        .emptyMessage(R.string.r_browser_nothing_to_display)
                        .resourceTypes(resourceTypes)
                        .resourceLabel(resource.getLabel())
                        .resourceUri(resource.getUri())
                        .build();
        fragment.getFragmentManager().beginTransaction()
                .addToBackStack(resource.getUri())
                .replace(R.id.resource_controller, newControllerFragment,
                        ResourcesControllerFragment.TAG + resource.getUri())
                .commit();
    }

    private void runReport(final ResourceLookup resource) {
        Account account = JasperAccountManager.get(activity).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(activity, account);
        String versionName = accountServerData.getVersionName();
        ServerRelease serverRelease = ServerRelease.parseVersion(accountServerData.getVersionName());

        switch (serverRelease) {
            case EMERALD:
            case EMERALD_MR1:
            case EMERALD_MR2:
            case EMERALD_MR3:
                ReportHtmlViewerActivity_.intent(activity)
                        .resource(resource).start();
                break;
            case AMBER:
            case AMBER_MR1:
            case AMBER_MR2:
                ReportViewerActivity_.intent(activity)
                        .resource(resource).start();
                break;
            default:
                throw new UnsupportedOperationException("Could not open viewer for current versionName: " + versionName);
        }
    }

    private void runDashboard(ResourceLookup resource) {
        SelectDashboardRenderDialog selectDashboardRenderDialog = new SelectDashboardRenderDialog();
        selectDashboardRenderDialog.setResource(resource);
        selectDashboardRenderDialog.show(activity.getSupportFragmentManager(), null);
    }

    public static class SelectDashboardRenderDialog extends DialogFragment {
        private ResourceLookup resource;

        public void setResource(ResourceLookup resource) {
            this.resource = resource;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select WebView render")
                    .setItems(new String[] {"Default", "Cordova"}, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                           switch (which) {
                               case 0:
                                   DashboardViewerActivity_.intent(getActivity())
                                           .resource(resource).start();
                                   break;
                               case 1:
                                   CordovaDashboardActivity_.intent(getActivity())
                                           .resource(resource).start();
                                   break;
                           }
                        }
                    });
            return builder.create();
        }
    }
}
