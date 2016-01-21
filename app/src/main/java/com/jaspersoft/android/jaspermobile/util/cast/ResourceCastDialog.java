/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.cast;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ResourceCastDialog extends MediaRouteControllerDialog implements ResourcePresentationService.ResourcePresentationCallback {

    private TextView resourceLabel;
    private ImageView resourceThumbnail;
    private ImageButton stopCastingResource;

    public ResourceCastDialog(Context context) {
        super(context, R.style.Theme_JasperMobile);
    }

    @Override
    public View onCreateMediaControlView(Bundle savedInstanceState) {
        LayoutInflater inflater = getLayoutInflater();
        View castView = inflater.inflate(R.layout.dialog_resource_cast, null);

        resourceLabel = ((TextView) castView.findViewById(R.id.resourceLabel));
        resourceThumbnail = ((ImageView) castView.findViewById(R.id.resourceThumbnail));
        resourceThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        stopCastingResource = (ImageButton) castView.findViewById(R.id.stopCastResource);
        stopCastingResource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResourcePresentationService().closeCurrentPresentation();
            }
        });

        return castView;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getResourcePresentationService().addResourcePresentationCallback(this);
        getResourcePresentationService().fetchState(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getResourcePresentationService().removeResourcePresentationCallback(this);
    }

    @Override
    public void onCastStarted() {
        resourceLabel.setText(R.string.r_pd_initializing_msg);
        resourceThumbnail.setVisibility(View.GONE);
        stopCastingResource.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onInitializationDone() {
        resourceLabel.setText(R.string.cast_ready_message);
        resourceThumbnail.setVisibility(View.GONE);
        stopCastingResource.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoadingStarted() {
        resourceLabel.setText(R.string.r_pd_running_report_msg);
        resourceThumbnail.setVisibility(View.GONE);
        stopCastingResource.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPresentationBegun() {
        resourceLabel.setText(getResourcePresentationService().getCurrentResourceLabel());
        stopCastingResource.setVisibility(View.VISIBLE);
        resourceThumbnail.setVisibility(View.VISIBLE);
        resourceThumbnail.setImageBitmap(getResourcePresentationService().getCurrentResourceThumbnail());
    }

    @Override
    public void onMultiPage() {

    }

    @Override
    public void onPageCountObtain(int pageCount) {

    }

    @Override
    public void onPageChanged(int pageNumb, String errorMessage) {

    }

    @Override
    public void onErrorOccurred(String errorMessage) {
        resourceLabel.setText(errorMessage);
        resourceThumbnail.setVisibility(View.GONE);
    }

    @Override
    public void onCastStopped() {
        dismiss();
    }

    private ResourcePresentationService getResourcePresentationService() {
        return (ResourcePresentationService) ResourcePresentationService.getInstance();
    }
}
