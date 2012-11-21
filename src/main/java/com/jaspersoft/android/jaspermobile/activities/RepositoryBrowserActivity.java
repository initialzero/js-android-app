/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jasperforge.org/projects/androidmobile
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.GetResourcesListAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorArrayAdapter;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorComparator;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class RepositoryBrowserActivity extends BaseRepositoryActivity implements JsOnTaskCallbackListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent(getIntent());
    }

    @Override
    public boolean onSearchRequested() {
        // Provide additional data in the intent that the system sends to the searchable activity
        Intent intent = getIntent();
        Bundle appData = new Bundle();
        appData.putString(EXTRA_BC_TITLE_SMALL, intent.getStringExtra(EXTRA_BC_TITLE_LARGE));
        appData.putString(EXTRA_RESOURCE_URI, intent.getStringExtra(EXTRA_RESOURCE_URI));
        // Passing search context data
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_OM_REFRESH:
                handleIntent(getIntent());
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleIntent(Intent intent) {
        //get extra pieces of data from intent
        Bundle extras = getIntent().getExtras();
        String titleSmall = extras.getString(EXTRA_BC_TITLE_SMALL);
        String titleLarge = extras.getString(EXTRA_BC_TITLE_LARGE);
        String uri = extras.getString(EXTRA_RESOURCE_URI);

        //update bread crumbs
        if (titleSmall != null && titleSmall.length() > 0) {
            breadCrumbsTitleSmall.setText(titleSmall);
            breadCrumbsTitleSmall.setVisibility(View.VISIBLE);
        }
        breadCrumbsTitleLarge.setText(titleLarge);

        // Create and run browse resources task
        jsAsyncTaskManager.executeTask(new GetResourcesListAsyncTask(GET_RESOURCE_TASK, getString(R.string.loading_msg),
                jsRestClient, uri));
    }

    //On success task complete handling
    @Override
    public void onTaskComplete(JsAsyncTask task) {
        super.onTaskComplete(task);

        switch (task.getId()) {
            case GET_RESOURCE_TASK:
                if (task.isCancelled()) {
                    // Report about resource canceling
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        List<ResourceDescriptor> resourceDescriptors = ((GetResourcesListAsyncTask)task).get();
                        if (resourceDescriptors.isEmpty()) {
                            // Show text that there are no resources in the folder
                            nothingToDisplayText.setText(R.string.r_browser_nothing_to_display);
                            nothingToDisplayText.setVisibility(View.VISIBLE);
                        } else {
                            nothingToDisplayText.setVisibility(View.GONE);
                            ResourceDescriptorArrayAdapter arrayAdapter = new ResourceDescriptorArrayAdapter(this, resourceDescriptors);
                            arrayAdapter.sort(new ResourceDescriptorComparator()); // sort: non-case-sensitive, folders first
                            setListAdapter(arrayAdapter);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            break;
        }
    }

    //On exception task complete handling
    public void onTaskException(JsAsyncTask task) {
        super.onTaskException(task);
    }
}
