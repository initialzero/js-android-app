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

package com.jaspersoft.android.jaspermobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.BaseRepositoryActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class ResourceInfoActivity extends RoboSherlockActivity {

    // Action Bar IDs
    private static final int ID_AB_PROGRESS = 10;
    private static final int ID_AB_SETTINGS = 11;

    @Inject
    protected JsRestClient jsRestClient;

    private SpiceManager serviceManager;
    private MenuItem indeterminateProgressItem;

    @InjectView(R.id.resource_name_info)    private TextView resourceName;
    @InjectView(R.id.resourceLabel)         private TextView resourceLabel;
    @InjectView(R.id.resourceDescription)   private TextView resourceDescription;
    @InjectView(R.id.resourceType)          private TextView resourceType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_info_layout);
        //update title
        getSupportActionBar().setTitle(R.string.ri_title);
        // bind to service
        serviceManager = new SpiceManager(JsXmlSpiceService.class);
        // get resource info
        setRefreshActionButtonState(true);
        String resourceUri = getIntent().getExtras().getString(BaseRepositoryActivity.EXTRA_RESOURCE_URI);
        GetResourceRequest request = new GetResourceRequest(jsRestClient, resourceUri);
        serviceManager.execute(request, request.createCacheKey(), DurationInMillis.ONE_HOUR, new GetResourceListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the App Icon for Navigation
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // indeterminate progress
        indeterminateProgressItem = menu.add(Menu.NONE, ID_AB_PROGRESS, Menu.NONE, R.string.r_ab_refresh);
        indeterminateProgressItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        indeterminateProgressItem.setActionView(R.layout.actionbar_indeterminate_progress);
        // settings
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void setRefreshActionButtonState(boolean refreshing) {
        if (indeterminateProgressItem != null) {
            indeterminateProgressItem.setVisible(refreshing);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetResourceListener implements RequestListener<ResourceDescriptor> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ResourceInfoActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ResourceDescriptor resourceDescriptor) {
            //update subtitle
            getSupportActionBar().setSubtitle(resourceDescriptor.getLabel());

            resourceName.setText(resourceDescriptor.getName());
            resourceLabel.setText(resourceDescriptor.getLabel());
            resourceDescription.setText(resourceDescriptor.getDescription());
            resourceType.setText(resourceDescriptor.getWsType().toString());

            setRefreshActionButtonState(false);
        }

    }

}
