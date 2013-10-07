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

package com.jaspersoft.android.jaspermobile.activities.resource;

import android.widget.TextView;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import roboguice.inject.InjectView;

/**
 * Activity that performs viewing of the resource info.
 *
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class ResourceInfoActivity extends BaseResourceActivity {

    @InjectView(R.id.resource_name_info)    private TextView resourceName;
    @InjectView(R.id.resourceLabel)         private TextView resourceLabel;
    @InjectView(R.id.resourceDescription)   private TextView resourceDescription;
    @InjectView(R.id.resourceType)          private TextView resourceType;

    protected void onGetResourceRequestSuccess(ResourceDescriptor descriptor) {
        resourceName.setText(descriptor.getName());
        resourceLabel.setText(descriptor.getLabel());
        resourceDescription.setText(descriptor.getDescription());
        resourceType.setText(descriptor.getWsType().toString());
    }

    protected int getContentViewResId() {
        return R.layout.resource_info_layout;
    }

    protected int getTitleResId() {
        return R.string.ri_title;
    }

}
