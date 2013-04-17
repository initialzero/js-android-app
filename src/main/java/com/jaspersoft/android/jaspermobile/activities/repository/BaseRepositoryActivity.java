/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.*;
import com.jaspersoft.android.jaspermobile.activities.async.AsyncTaskExceptionHandler;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsAsyncTaskManager;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.DeleteResourceAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorArrayAdapter;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public abstract class BaseRepositoryActivity extends RoboSherlockListActivity implements JsOnTaskCallbackListener{

    // Extras
    public static final String EXTRA_BC_TITLE_SMALL = "BaseRepositoryActivity.EXTRA_BC_TITLE_SMALL";
    public static final String EXTRA_BC_TITLE_LARGE = "BaseRepositoryActivity.EXTRA_BC_TITLE_LARGE";
    public static final String EXTRA_RESOURCE_URI = "BaseRepositoryActivity.EXTRA_RESOURCE_URI";
    // Context menu IDs
    protected static final int ID_CM_OPEN = 20;
    protected static final int ID_CM_RUN = 21;
    protected static final int ID_CM_EDIT = 22;
    protected static final int ID_CM_DELETE = 23;
    protected static final int ID_CM_VIEW_DETAILS = 24;
    protected static final int ID_CM_ADD_TO_FAVORITES = 25;

    //Async task identifiers
    public static final int DELETE_RESOURCE_TASK = 1;
    public static final int GET_RESOURCE_TASK = 2;
    public static final int SEARCH_RESOURCES_TASK = 3;

    @InjectView(R.id.nothingToDisplayText)      protected TextView nothingToDisplayText;
    @InjectView(android.R.id.list)              protected ListView listView;

    @Inject protected JsRestClient jsRestClient;

    protected JsAsyncTaskManager jsAsyncTaskManager;

    protected DatabaseProvider dbProvider;

    private ResourceDescriptor resourceDescriptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the database provider
        dbProvider = new DatabaseProvider(this);

        // Create manager and set this activity as context and listener
        jsAsyncTaskManager = new JsAsyncTaskManager(this, this);

        // Handle tasks that can be retained before
        jsAsyncTaskManager.handleRetainedTasks((List<JsAsyncTask>) getLastNonConfigurationInstance());

        setContentView(R.layout.repository_layout);
        // Register a context menu to be shown for the given view
        registerForContextMenu(listView);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Delegate tasks retain to manager
        return jsAsyncTaskManager.retainTasks();
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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
                menu.add(Menu.NONE, ID_CM_OPEN, Menu.NONE, R.string.r_cm_open);
                break;
            case reportUnit:
            case dashboard:
                menu.add(Menu.NONE, ID_CM_RUN, Menu.NONE, R.string.r_cm_run);
                break;
        }

        menu.add(Menu.NONE, ID_CM_EDIT, Menu.NONE, R.string.r_cm_edit);
        menu.add(Menu.NONE, ID_CM_DELETE, Menu.NONE, R.string.r_cm_delete);
        menu.add(Menu.NONE, ID_CM_VIEW_DETAILS, Menu.NONE, R.string.r_cm_view_details);
        menu.add(Menu.NONE, ID_CM_ADD_TO_FAVORITES, Menu.NONE, R.string.r_cm_add_to_favorites);
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
            case ID_CM_DELETE:
                showDialog(ID_CM_DELETE);
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
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        Bundle extras = data.getExtras();

        switch (requestCode) {
            case ID_CM_EDIT: {
                if (resultCode == RESULT_OK) {
                    //Refresh current repository resource.
                    resourceDescriptor.setLabel(extras.getString(ResourceEditActivity.RESOURCE_LABEL));
                    resourceDescriptor.setDescription(extras.getString(ResourceEditActivity.RESOURCE_DESCRIPTION));
                    ((ResourceDescriptorArrayAdapter)getListAdapter()).notifyDataSetChanged();
                } else if (resultCode == ResourceEditActivity.RESULT_ERROR_ACCESS_DENIED) {
                    showErrorDialog(R.string.error_http_403);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case ID_CM_DELETE:
                // Define the delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.r_ad_delete_resource_msg)
                        // the delete button handler
                        .setPositiveButton(R.string.r_delete_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Create and run modify resource task and proper progress dialog
                                jsAsyncTaskManager.executeTask(new DeleteResourceAsyncTask(DELETE_RESOURCE_TASK, getString(R.string.r_pd_deleting_msg),
                                        jsRestClient, resourceDescriptor.getUriString()));
                            }
                        })
                        // the cancel button handler
                        .setNegativeButton(R.string.r_cancel_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void showErrorDialog(int messageId) {
        // prepare the alert box
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setTitle(R.string.error_msg).setIcon(android.R.drawable.ic_dialog_alert);

        // set the message to display
        alertbox.setMessage(messageId);

        // add a neutral button to the alert box and assign a click listener
        alertbox.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            // click listener on the alert box
            public void onClick(DialogInterface arg0, int arg1) {
                // do nothing
            }
        });

        alertbox.show();
    }

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
        Intent intent = new Intent();
        intent.setClass(this, ReportOptionsActivity.class);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_LABEL , reportLabel);
        intent.putExtra(ReportOptionsActivity.EXTRA_REPORT_URI , reportUri);
        startActivity(intent);
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

    //On success task complete handling
    public void onTaskComplete(JsAsyncTask task) {
        switch (task.getId()) {
            case DELETE_RESOURCE_TASK:
                //Refresh repository resources
                ((ResourceDescriptorArrayAdapter) getListAdapter()).remove(resourceDescriptor);
                ((ResourceDescriptorArrayAdapter) getListAdapter()).notifyDataSetChanged();
                // the feedback about an operation in a small popup
                Toast.makeText(getApplicationContext(), R.string.r_resource_deleted_toast, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //On exception task complete handling
    public void onTaskException(JsAsyncTask task) {
        AsyncTaskExceptionHandler.handle(task, this, true);
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }
}
