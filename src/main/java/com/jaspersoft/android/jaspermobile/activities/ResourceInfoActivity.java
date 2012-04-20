/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsAsyncTaskManager;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.GetResourceAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceProperty;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.AsyncTaskExceptionHandler;
import roboguice.activity.RoboActivity;
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
public class ResourceInfoActivity extends RoboActivity implements JsOnTaskCallbackListener {

    private JsAsyncTaskManager jsAsyncTaskManager;

    @Inject protected JsRestClient jsRestClient;

    // Async Task IDs
    private static final int GET_RESOURCE_TASK = 1;

    @InjectView(R.id.breadcrumbs_title_small)       private TextView breadCrumbsTitleSmall;
    @InjectView(R.id.breadcrumbs_title_large)       private TextView breadCrumbsTitleLarge;
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
                getString(R.string.r_pd_loading_msg), jsRestClient, resourceUri);
        jsAsyncTaskManager.executeTask(getResourceAsyncTask);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Delegate tasks retain to manager
        return jsAsyncTaskManager.retainTasks();
    }

    public void BackToRepositoryClickHandler(View v) {
        finish();
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
                    ResourceDescriptor resourceDescriptor = null;
                    try {
                        resourceDescriptor = ((GetResourceAsyncTask)task).get();
                    } catch (InterruptedException e) {
                        //TODO
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        //TODO
                        e.printStackTrace();
                    }

                    //update bread crumbs
                    breadCrumbsTitleSmall.setText(resourceDescriptor.getLabel());
                    breadCrumbsTitleSmall.setVisibility(View.VISIBLE);
                    breadCrumbsTitleLarge.setText(getString(R.string.ri_title));

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


    public void actionButtonOnClickListener(View view) {
        switch (view.getId()) {
            case R.id.app_icon_button:
                HomeActivity.goHome(this);
        }
    }

}
