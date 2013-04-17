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

package com.jaspersoft.android.jaspermobile.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.AsyncTaskExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.BaseRepositoryActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsAsyncTaskManager;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.GetResourceAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceProperty;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Volodya Sabadosh (vsabadosh@jaspersoft.com)
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ResourceInfoActivity extends RoboSherlockActivity implements JsOnTaskCallbackListener {

    private JsAsyncTaskManager jsAsyncTaskManager;

    @Inject protected JsRestClient jsRestClient;

    // Async Task IDs
    private static final int GET_RESOURCE_TASK = 1;

    @InjectView(R.id.resource_name_info)            private TextView resourceName;
    @InjectView(R.id.resourceLabel)                 private TextView resourceLabel;
    @InjectView(R.id.resourceDescription)           private TextView resourceDescription;
    @InjectView(R.id.resourceType)                  private TextView resourceType;
    @InjectView(android.R.id.list)                  private ListView propertiesListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_info_layout);

        // Create manager and set this activity as context and listener
        jsAsyncTaskManager = new JsAsyncTaskManager(this, this);

        // Handle tasks that can be retained before
        jsAsyncTaskManager.handleRetainedTasks((List<JsAsyncTask>) getLastNonConfigurationInstance());

        Bundle extras = getIntent().getExtras();

        String resourceUri = extras.getString(BaseRepositoryActivity.EXTRA_RESOURCE_URI);

        // Create and run getting resource task and proper progress dialog
        GetResourceAsyncTask getResourceAsyncTask = new GetResourceAsyncTask(GET_RESOURCE_TASK,
                getString(R.string.loading_msg), jsRestClient, resourceUri);
        jsAsyncTaskManager.executeTask(getResourceAsyncTask);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Delegate tasks retain to manager
        return jsAsyncTaskManager.retainTasks();
    }

    //On success async task complete handling
    public void onTaskComplete(JsAsyncTask task) {
        switch (task.getId()) {
            case GET_RESOURCE_TASK:
                if (task.isCancelled()) {
                    // Report about resource canceling
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    ResourceDescriptor resourceDescriptor;
                    try {
                        resourceDescriptor = ((GetResourceAsyncTask)task).get();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }

                    //update titles
                    getSupportActionBar().setTitle(R.string.ri_title);
                    getSupportActionBar().setSubtitle(resourceDescriptor.getLabel());


                    resourceName.setText(resourceDescriptor.getName());
                    resourceLabel.setText(resourceDescriptor.getLabel());
                    resourceDescription.setText(resourceDescriptor.getDescription());
                    resourceType.setText(resourceDescriptor.getWsType().toString());

                    // populate some resource properties into list properties
                    ArrayList<HashMap<String, String>> propertiesList = new ArrayList<HashMap<String, String>>();
                    for (ResourceProperty resourceProperty : resourceDescriptor.getProperties()) {
                        HashMap propertyMap = new HashMap();
                        propertyMap.put("property_name", resourceProperty.getName());
                        propertyMap.put("property_value", resourceProperty.getValue());
                        propertiesList.add(propertyMap);
                    }

                    SimpleAdapter resourcePropertyAdapter = new SimpleAdapter(
                            getApplicationContext(),
                            propertiesList,
                            R.layout.resource_property_layout,
                            new String[] {"property_name", "property_value"}, new int[] {R.id.resource_propertyName,
                            R.id.resource_propertyValue});

                    propertiesListView.setAdapter(resourcePropertyAdapter);
                }
                break;
        }
    }

    //On exception task complete handling
    public void onTaskException(JsAsyncTask task) {
        AsyncTaskExceptionHandler.handle(task, this, true);
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
                finish();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

}
