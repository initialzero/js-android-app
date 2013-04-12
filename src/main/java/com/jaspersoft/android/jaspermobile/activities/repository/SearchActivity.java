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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.AsyncTaskExceptionHandler;
import com.jaspersoft.android.sdk.client.async.JsOnTaskCallbackListener;
import com.jaspersoft.android.sdk.client.async.task.JsAsyncTask;
import com.jaspersoft.android.sdk.client.async.task.SearchResourcesAsyncTask;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorArrayAdapter;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorComparator;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class SearchActivity extends BaseRepositoryActivity implements JsOnTaskCallbackListener {

    // Extras
    public static final String EXTRA_RESOURCE_TYPES = "SearchActivity.EXTRA_RESOURCE_TYPES";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onSearchRequested() {
        // Provide additional data in the intent that sends to the searchable activity
        Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
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
        // Get search query from extras
        String query = intent.getStringExtra(SearchManager.QUERY);
        // Get additional data from intent
        Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
        String titleSmall = appData.getString(EXTRA_BC_TITLE_SMALL);
        String titleLarge = getString(R.string.s_title);
        String uri = appData.getString(EXTRA_RESOURCE_URI);
        ArrayList<String> types = appData.getStringArrayList(EXTRA_RESOURCE_TYPES);
        //update bread crumbs
        if (titleSmall != null && titleSmall.length() > 0) {
            breadCrumbsTitleSmall.setText(titleSmall);
            breadCrumbsTitleSmall.setVisibility(View.VISIBLE);
        }
        breadCrumbsTitleLarge.setText(titleLarge);

        // Create and run search resources task
        jsAsyncTaskManager.executeTask(new SearchResourcesAsyncTask(SEARCH_RESOURCES_TASK, getString(R.string.s_pd_searching_msg),
                jsRestClient, uri, query, types, true, 0));
    }

    public void onTaskComplete(JsAsyncTask task) {
        switch (task.getId()) {
            case SEARCH_RESOURCES_TASK:
                if (task.isCancelled()) {
                    // Report about resource canceling
                    Toast.makeText(this, R.string.cancelled_msg, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    try {
                        List<ResourceDescriptor> resourceDescriptors = ((SearchResourcesAsyncTask)task).get();
                        if (resourceDescriptors.isEmpty()) {
                            // Show text that there are no results from search
                            nothingToDisplayText.setText(R.string.r_search_nothing_to_display);
                            nothingToDisplayText.setVisibility(View.VISIBLE);
                        } else {
                            nothingToDisplayText.setVisibility(View.GONE);
                            ResourceDescriptorArrayAdapter arrayAdapter = new ResourceDescriptorArrayAdapter(this, resourceDescriptors);
                            // sort the search results
                            arrayAdapter.sort(new ResourceDescriptorComparator());
                            setListAdapter(arrayAdapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    @Override
    public void onTaskException(JsAsyncTask task) {
        Exception exception = task.getTaskException();
        SearchResourcesAsyncTask searchTask = (SearchResourcesAsyncTask) task;
        // workaround for JRS CE (it doesn't know about dashboards)
        if (exception != null
            && exception instanceof HttpStatusCodeException
            && ((HttpStatusCodeException) exception).getStatusCode() == HttpStatus.BAD_REQUEST
            && searchTask.getTypes().remove(ResourceDescriptor.WsType.dashboard.toString())) {
                SearchResourcesAsyncTask newTask = new SearchResourcesAsyncTask(SEARCH_RESOURCES_TASK,
                        getString(R.string.s_pd_searching_msg), jsRestClient, searchTask.getResourceUri(),
                        searchTask.getQuery(), searchTask.getTypes(), true, 0);
                jsAsyncTaskManager.executeTask(newTask);
        } else {
            AsyncTaskExceptionHandler.handle(task, this, true);
        }
    }

}
