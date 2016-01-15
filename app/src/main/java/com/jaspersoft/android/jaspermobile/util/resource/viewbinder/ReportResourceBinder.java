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

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import roboguice.RoboGuice;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class ReportResourceBinder extends ResourceBinder {

    @Inject
    protected JsRestClient jsRestClient;

    private ImageView thumbnail;

    public ReportResourceBinder(Context context) {
        super(context);
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);
    }

    @Override
    public void setIcon(TopCropImageView imageView, JasperResource jasperResource) {
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setScaleType(TopCropImageView.ScaleType.FIT_CENTER);

        if (jasperResource.getResourceType() == JasperResourceType.report) {
            String thumbnailUri = ((ReportResource) jasperResource).getThumbnailUri();
            thumbnail = imageView;
            loadFromNetwork(thumbnailUri);
        }
    }

    @Override
    public void unbindView(TopCropImageView imageView) {
        ImageLoader.getInstance().cancelDisplayTask(imageView);
    }

    private void loadFromNetwork(String uri) {
        ImageLoader.getInstance().displayImage(
                uri, thumbnail, getDisplayImageOptions(),
                new ThumbnailLoadingListener());
    }

    private DisplayImageOptions getDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder_report)
                .showImageForEmptyUri(R.drawable.placeholder_report)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private static class ThumbnailLoadingListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (view != null) {
                ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.TOP_CROP);
            }
        }
    }

}
