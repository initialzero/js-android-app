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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.activities.repository.BaseRepositoryActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsAsyncTaskManager;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.GetResourceAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.ModifyResourceAsyncTask;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.jaspermobile.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * @author Volodya Sabadosh (vsabadosh@jaspersoft.com)
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class ResourceEditActivity extends RoboActivity implements JsOnTaskCallbackListener {

    public static final String RESOURCE_LABEL = "ResourceEditActivity.LABEL";
    public static final String RESOURCE_DESCRIPTION = "ResourceEditActivity.DESCRIPTION";

    // TODO: review this stuff
    public static final int RESULT_ERROR = -2;
    public static final int RESULT_ERROR_ACCESS_DENIED = -3;

    // Async Task IDs
    private static final int GET_RESOURCE_TASK = 1;
    private static final int MODIFY_RESOURCE_TASK = 2;

    private JsAsyncTaskManager jsAsyncTaskManager;

    @Inject protected JsRestClient jsRestClient;

    @InjectView(R.id.breadcrumbs_title_small)   private TextView breadCrumbsTitleSmall;
    @InjectView(R.id.breadcrumbs_title_large)   private TextView breadCrumbsTitleLarge;
    @InjectView(R.id.resourceLabel)         private EditText resourceLabel;
    @InjectView(R.id.resourceDescription)   private EditText resourceDescription;

    private ResourceDescriptor resourceDescriptor;
    private String resourceUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_edit_layout);
        //update bread crumbs
        breadCrumbsTitleLarge.setText(getString(R.string.re_title));

        Bundle extras = getIntent().getExtras();

        resourceUri = extras.getString(BaseRepositoryActivity.EXTRA_RESOURCE_URI);

        // Create manager and set this activity as context and listener
        jsAsyncTaskManager = new JsAsyncTaskManager(this, this);

        // Handle task that can be retained before
        jsAsyncTaskManager.handleRetainedTasks((List<JsAsyncTask>) getLastNonConfigurationInstance());

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

    public void modifyResourceClickHandler(View v) {
        //Updates current resource descriptor with edit resource UI values
        resourceDescriptor.setLabel(resourceLabel.getText().toString());
        resourceDescriptor.setDescription(resourceDescription.getText().toString());

        // Create and run modify resource task and proper progress dialog
        jsAsyncTaskManager.executeTask(new ModifyResourceAsyncTask(MODIFY_RESOURCE_TASK,
                getString(R.string.re_pd_saving_msg), jsRestClient, resourceDescriptor));

    }

    //On success async task complete handling
    public void onTaskComplete(JsAsyncTask task) {
        switch (task.getId()) {
            case MODIFY_RESOURCE_TASK:
                if (task.isCancelled()) {
                    // Report about resource canceling
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(ResourceEditActivity.RESOURCE_LABEL, resourceLabel.getText().toString());
                    intent.putExtra(ResourceEditActivity.RESOURCE_DESCRIPTION, resourceDescription.getText().toString());
                    setResult(RESULT_OK, intent);
                    Toast.makeText(this, R.string.re_resource_saved_toast, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
            case GET_RESOURCE_TASK:
                 if (task.isCancelled()) {
                     // Report about resource canceling
                     Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT)
                             .show();
                     finish();
                 } else {
                     try {
                         this.resourceDescriptor = ((GetResourceAsyncTask)task).get();
                         //update bread crumbs
                         breadCrumbsTitleSmall.setText(resourceDescriptor.getLabel());
                         breadCrumbsTitleSmall.setVisibility(View.VISIBLE);
                         //Inites edit resource UI with resource descriptor information.
                         resourceLabel.setText(resourceDescriptor.getLabel());
                         resourceDescription.setText(resourceDescriptor.getDescription());
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
        }
    }

    //On exception task complete handling
    public void onTaskException(JsAsyncTask task) {
        switch (task.getId()) {
            case MODIFY_RESOURCE_TASK:
                Exception resultError = task.getTaskException();

                Intent intent = new Intent();
                if (resultError instanceof HttpClientErrorException &&
                        ((HttpClientErrorException)resultError).getStatusCode() == HttpStatus.FORBIDDEN)
                    setResult(RESULT_ERROR_ACCESS_DENIED, intent);
                else {
                    setResult(RESULT_ERROR, intent);
                }
                finish();
            case GET_RESOURCE_TASK:
                //TODO
        }
    }

    public void actionButtonOnClickListener(View view) {
        switch (view.getId()) {
            case R.id.app_icon_button:
                HomeActivity.goHome(this);
        }
    }

}
