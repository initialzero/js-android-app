/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.*;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.report.BaseReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.CompatReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.report.ReportOptionsActivity;
import com.jaspersoft.android.jaspermobile.activities.resource.ResourceEditActivity;
import com.jaspersoft.android.jaspermobile.activities.resource.ResourceInfoActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.DashboardHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetServerInfoRequest;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorArrayAdapter;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public abstract class BaseRepositoryActivity extends RoboSherlockListActivity {

    // Extras
    public static final String EXTRA_BC_TITLE_SMALL = "BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL";
    public static final String EXTRA_BC_TITLE_LARGE = "BaseRepositoryActivity.EXTRA_BC_TITLE_LARGE";
    public static final String EXTRA_RESOURCE_URI = "BaseRepositoryActivity.EXTRA_RESOURCE_URI";
    // Context menu IDs
    protected static final int ID_CM_OPEN = 10;
    protected static final int ID_CM_RUN = 11;
    protected static final int ID_CM_EDIT = 12;
    protected static final int ID_CM_DELETE = 13;
    protected static final int ID_CM_VIEW_DETAILS = 14;
    protected static final int ID_CM_ADD_TO_FAVORITES = 15;

    // Action Bar IDs
    private static final int ID_AB_SETTINGS = 30;

    @InjectView(R.id.nothingToDisplayText)      protected TextView nothingToDisplayText;
    @InjectView(android.R.id.list)              protected ListView listView;

    @Inject
    protected JsRestClient jsRestClient;
    protected DatabaseProvider dbProvider;
    protected SpiceManager serviceManager;

    protected ResourceDescriptor resourceDescriptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repository_layout);
        // set empty view
        listView.setEmptyView(nothingToDisplayText);
        // Get the database provider
        dbProvider = new DatabaseProvider(this);
        // Register a context menu to be shown for the given view
        registerForContextMenu(listView);
        // bind to service
        serviceManager = new SpiceManager(JsXmlSpiceService.class);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        ResourceDescriptor resourceDescriptor = (ResourceDescriptor) getListView().getItemAtPosition(position);
        switch (resourceDescriptor.getWsType()) {
            case folder:
                openFolderByDescriptor(resourceDescriptor);
                break;
            case reportUnit:
                runReport(resourceDescriptor.getLabel(), resourceDescriptor.getUriString());
                break;
            case dashboard:
                runDashboard(resourceDescriptor.getUriString());
                break;
            default:
                viewResource(resourceDescriptor.getUriString());
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the App Icon for Navigation
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Add actions to the action bar
        menu.add(Menu.NONE, ID_AB_SETTINGS, Menu.NONE, R.string.ab_settings)
                .setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_SETTINGS:
                // Launch the settings activity
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case android.R.id.home:
                HomeActivity.goHome(this);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        // Determine on which item in the ListView the user long-clicked and get corresponding resource descriptor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ResourceDescriptor resourceDescriptor = (ResourceDescriptor) getListView().getItemAtPosition(info.position);

        // Retrieve the label for that particular item and use as title for the menu
        menu.setHeaderTitle(resourceDescriptor.getLabel());
        // Add all the menu options
        switch (resourceDescriptor.getWsType()) {
            case folder:
                menu.add(Menu.NONE, ID_CM_OPEN, Menu.FIRST, R.string.r_cm_open);
                break;
            case reportUnit:
            case dashboard:
                menu.add(Menu.NONE, ID_CM_RUN, Menu.FIRST, R.string.r_cm_run);
                break;
        }

        menu.add(Menu.NONE, ID_CM_EDIT, Menu.FIRST, R.string.r_cm_edit);
        menu.add(Menu.NONE, ID_CM_VIEW_DETAILS, Menu.CATEGORY_SECONDARY, R.string.r_cm_view_details);
        menu.add(Menu.NONE, ID_CM_ADD_TO_FAVORITES, Menu.CATEGORY_SECONDARY, R.string.r_cm_add_to_favorites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Determine on which item in the ListView the user long-clicked and get corresponding resource descriptor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        resourceDescriptor = (ResourceDescriptor) getListView().getItemAtPosition(info.position);

        // Handle item selection
        switch (item.getItemId()) {
            case ID_CM_OPEN:
                openFolderByDescriptor(resourceDescriptor);
                return true;
            case ID_CM_RUN:
                switch (resourceDescriptor.getWsType()) {
                    case reportUnit:
                        runReport(resourceDescriptor.getLabel(), resourceDescriptor.getUriString());
                        break;
                    case dashboard:
                        runDashboard(resourceDescriptor.getUriString());
                        break;
                }
                return true;
            case ID_CM_EDIT:
                editResource(resourceDescriptor.getUriString());
                return true;
            case ID_CM_VIEW_DETAILS:
                viewResource(resourceDescriptor.getUriString());
                return true;
            case ID_CM_ADD_TO_FAVORITES:
                String label = resourceDescriptor.getLabel();
                String name = resourceDescriptor.getName();
                String uri = resourceDescriptor.getUriString();
                String description = resourceDescriptor.getDescription();
                String wsType = resourceDescriptor.getWsType().toString();
                String userName = jsRestClient.getServerProfile().getUsername();
                String organization = jsRestClient.getServerProfile().getOrganization();
                long serverProfileId = jsRestClient.getServerProfile().getId();
                dbProvider.insertFavoriteItem(label, name, uri, description, wsType, serverProfileId, userName, organization);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ID_CM_EDIT && resultCode == RESULT_OK && data != null) {
            // refresh current repository resource
            Bundle extras = data.getExtras();
            resourceDescriptor.setLabel(extras.getString(ResourceEditActivity.EXTRA_RESOURCE_LABEL));
            resourceDescriptor.setDescription(extras.getString(ResourceEditActivity.EXTRA_RESOURCE_DESCRIPTION));
            ((ResourceDescriptorArrayAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        serviceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        serviceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void openFolderByDescriptor(ResourceDescriptor resourceDescriptor) {
        Intent intent = new Intent();
        intent.setClass(this, BrowserActivity.class);
        intent.putExtra(EXTRA_BC_TITLE_SMALL, getIntent().getExtras().getString(EXTRA_BC_TITLE_LARGE));
        intent.putExtra(EXTRA_BC_TITLE_LARGE, resourceDescriptor.getLabel());
        intent.putExtra(EXTRA_RESOURCE_URI , resourceDescriptor.getUriString());
        startActivity(intent);
    }

    private void editResource(String resourceUri) {
        Intent intent = new Intent();
        intent.setClass(this, ResourceEditActivity.class);
        intent.putExtra(EXTRA_BC_TITLE_SMALL, getIntent().getExtras().getString(EXTRA_BC_TITLE_LARGE));
        intent.putExtra(EXTRA_RESOURCE_URI , resourceUri);
        startActivityForResult(intent, ID_CM_EDIT);
    }

    private void viewResource(String resourceUri) {
        Intent intent = new Intent();
        intent.setClass(this, ResourceInfoActivity.class);
        intent.putExtra(EXTRA_BC_TITLE_SMALL, getIntent().getExtras().getString(EXTRA_BC_TITLE_LARGE));
        intent.putExtra(EXTRA_RESOURCE_URI , resourceUri);
        startActivityForResult(intent, ID_CM_VIEW_DETAILS);
    }

    private void runReport(String reportLabel, String reportUri) {
        GetServerInfoRequest request = new GetServerInfoRequest(jsRestClient);
        GetServerInfoListener listener = new GetServerInfoListener(reportLabel, reportUri);
        long cacheExpiryDuration = SettingsActivity.getRepoCacheExpirationValue(this);
        serviceManager.execute(request, request.createCacheKey(), cacheExpiryDuration, listener);
    }

    private void runDashboard(String dashboardUri) {
        // generate url
        String dashboardUrl = jsRestClient.getServerProfile().getServerUrl()
                + "/flow.html?_flowId=dashboardRuntimeFlow&viewAsDashboardFrame=true&dashboardResource="
                + dashboardUri;
        // run the html dashboard viewer
        Intent htmlViewer = new Intent();
        htmlViewer.setClass(this, DashboardHtmlViewerActivity.class);
        htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URL, dashboardUrl);
        startActivity(htmlViewer);
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetServerInfoListener implements RequestListener<ServerInfo> {

        private String reportLabel;
        private String reportUri;

        public GetServerInfoListener(String reportLabel, String reportUri) {
            this.reportLabel = reportLabel;
            this.reportUri = reportUri;
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            RequestExceptionHandler.handle(e, BaseRepositoryActivity.this, false);
        }

        @Override
        public void onRequestSuccess(ServerInfo serverInfo) {
            Class clazz = (serverInfo.getVersionCode() < ServerInfo.VERSION_CODES.EMERALD)
                    ? CompatReportOptionsActivity.class : ReportOptionsActivity.class;
            // start new activity
            Intent intent = new Intent();
            intent.setClass(BaseRepositoryActivity.this, clazz);
            intent.putExtra(BaseReportOptionsActivity.EXTRA_REPORT_LABEL , reportLabel);
            intent.putExtra(BaseReportOptionsActivity.EXTRA_REPORT_URI , reportUri);
            startActivity(intent);
        }
    }

}
