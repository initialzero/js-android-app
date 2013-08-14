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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.activities.repository.BaseRepositoryActivity;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.ModifyResourceRequest;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class ResourceEditActivity extends RoboSherlockActivity {

    public static final String EXTRA_RESOURCE_LABEL = "ResourceEditActivity.EXTRA_RESOURCE_LABEL";
    public static final String EXTRA_RESOURCE_DESCRIPTION = "ResourceEditActivity.EXTRA_RESOURCE_DESCRIPTION";

    // Action Bar IDs
    private static final int ID_AB_PROGRESS = 10;
    private static final int ID_AB_SETTINGS = 11;

    @Inject protected JsRestClient jsRestClient;

    private SpiceManager serviceManager;
    private MenuItem indeterminateProgressItem;
    private ResourceDescriptor resourceDescriptor;

    @InjectView(R.id.resourceLabel)         private EditText resourceLabel;
    @InjectView(R.id.resourceDescription)   private EditText resourceDescription;
    @InjectView(R.id.modifyResourceButton)  private Button modifyResourceButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_edit_layout);
        //update title
        getSupportActionBar().setTitle(R.string.re_title);
        // bind to service
        serviceManager = new SpiceManager(JsXmlSpiceService.class);
        // get resource info
        setRefreshActionButtonState(true);
        String resourceUri = getIntent().getExtras().getString(BaseRepositoryActivity.EXTRA_RESOURCE_URI);
        GetResourceRequest request = new GetResourceRequest(jsRestClient, resourceUri);
        serviceManager.execute(request, new GetResourceListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the App Icon for Navigation
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // indeterminate progress
        indeterminateProgressItem = menu.add(Menu.NONE, ID_AB_PROGRESS, Menu.NONE, R.string.r_ab_refresh);
        indeterminateProgressItem.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
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

    public void modifyResourceClickHandler(View view) {
        // update current resource descriptor
        resourceDescriptor.setLabel(resourceLabel.getText().toString());
        resourceDescriptor.setDescription(resourceDescription.getText().toString());
        // execute request
        setRefreshActionButtonState(true);
        ModifyResourceRequest request = new ModifyResourceRequest(jsRestClient, resourceDescriptor);
        serviceManager.execute(request, new ModifyResourceListener());
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
        modifyResourceButton.setEnabled(!refreshing);
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class GetResourceListener implements RequestListener<ResourceDescriptor> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ResourceEditActivity.this, true);
        }

        @Override
        public void onRequestSuccess(ResourceDescriptor descriptor) {
            resourceDescriptor = descriptor;
            //update subtitle
            getSupportActionBar().setSubtitle(resourceDescriptor.getLabel());
            // update label and description
            resourceLabel.setText(resourceDescriptor.getLabel());
            resourceDescription.setText(resourceDescriptor.getDescription());

            setRefreshActionButtonState(false);
        }

    }

    private class ModifyResourceListener implements RequestListener<Void> {

        @Override
        public void onRequestFailure(SpiceException exception) {
            RequestExceptionHandler.handle(exception, ResourceEditActivity.this, true);
        }

        @Override
        public void onRequestSuccess(Void result) {
            Intent intent = new Intent();
            intent.putExtra(ResourceEditActivity.EXTRA_RESOURCE_LABEL, resourceLabel.getText().toString());
            intent.putExtra(ResourceEditActivity.EXTRA_RESOURCE_DESCRIPTION, resourceDescription.getText().toString());
            setResult(RESULT_OK, intent);
            Toast.makeText(ResourceEditActivity.this, R.string.re_resource_saved_toast, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

}
