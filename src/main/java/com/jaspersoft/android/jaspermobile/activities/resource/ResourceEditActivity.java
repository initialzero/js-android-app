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

package com.jaspersoft.android.jaspermobile.activities.resource;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.async.RequestExceptionHandler;
import com.jaspersoft.android.sdk.client.async.request.ModifyResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import roboguice.inject.InjectView;

/**
 * Activity that performs resource modifying.
 *
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class ResourceEditActivity extends BaseResourceActivity {

    public static final String EXTRA_RESOURCE_LABEL = "ResourceEditActivity.EXTRA_RESOURCE_LABEL";
    public static final String EXTRA_RESOURCE_DESCRIPTION = "ResourceEditActivity.EXTRA_RESOURCE_DESCRIPTION";

    private ResourceDescriptor resourceDescriptor;

    @InjectView(R.id.resourceLabel)         private EditText resourceLabel;
    @InjectView(R.id.resourceDescription)   private EditText resourceDescription;
    @InjectView(R.id.modifyResourceButton)  private Button modifyResourceButton;

    public void modifyResourceClickHandler(View view) {
        // update current resource descriptor
        resourceDescriptor.setLabel(resourceLabel.getText().toString());
        resourceDescriptor.setDescription(resourceDescription.getText().toString());
        // execute request
        setRefreshActionButtonState(true);
        ModifyResourceRequest request = new ModifyResourceRequest(jsRestClient, resourceDescriptor);
        serviceManager.execute(request, new ModifyResourceListener());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    @Override
    protected void setRefreshActionButtonState(boolean refreshing) {
        super.setRefreshActionButtonState(refreshing);
        modifyResourceButton.setEnabled(!refreshing);
    }

    protected void onGetResourceRequestSuccess(ResourceDescriptor descriptor) {
        resourceDescriptor = descriptor;
        // update label and description
        resourceLabel.setText(resourceDescriptor.getLabel());
        resourceDescription.setText(resourceDescriptor.getDescription());
    }

    protected int getContentViewResId() {
        return R.layout.resource_edit_layout;
    }

    protected int getTitleResId() {
        return R.string.re_title;
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

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
