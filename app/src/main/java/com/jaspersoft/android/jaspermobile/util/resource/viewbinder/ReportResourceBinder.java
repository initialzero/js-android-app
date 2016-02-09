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

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;


/**
 * @author Tom Koptel
 * @since 1.9
 */
class ReportResourceBinder extends ResourceBinder {
    private ImageView thumbnail;

    public ReportResourceBinder(Context context) {
        super(context);
    }

    @Override
    public void setIcon(ImageView imageView, JasperResource jasperResource) {
        imageView.setBackgroundResource(R.drawable.bg_resource_icon_grey);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadFromNetwork(imageView, jasperResource, getDisplayImageOptions(R.drawable.ic_report));
    }

    @Override
    public void setThumbnail(ImageView imageView, JasperResource jasperResource) {
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        loadFromNetwork(imageView, jasperResource, getDisplayImageOptions(R.drawable.im_thumbnail_report));
    }

    private void loadFromNetwork(ImageView imageView, JasperResource jasperResource, DisplayImageOptions displayImageOptions) {
        if (jasperResource.getResourceType() == JasperResourceType.report) {
            String thumbnailUri = ((ReportResource) jasperResource).getThumbnailUri();
            thumbnail = imageView;
            ImageLoader.getInstance().displayImage(thumbnailUri, thumbnail, displayImageOptions);
        }
    }

    private DisplayImageOptions getDisplayImageOptions(int placeholderResource) {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(placeholderResource)
                .showImageForEmptyUri(placeholderResource)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        int scaledWidth = (int) (bitmap.getWidth() * 0.66);
                        int newHeight = scaledWidth < bitmap.getHeight() ? scaledWidth : bitmap.getHeight();
                        return Bitmap.createBitmap(bitmap, 3, 3, bitmap.getWidth() - 6, newHeight - 6);
                    }
                })
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
}
