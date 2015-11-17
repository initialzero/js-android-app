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
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import roboguice.RoboGuice;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class ReportResourceBinder extends ResourceBinder {
    private final boolean isAmberOrHigher;

    @Inject
    protected JsRestClient jsRestClient;

    public ReportResourceBinder(Context context) {
        super(context);
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);

        Account account = JasperAccountManager.get(context).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(context, account);
        ServerRelease serverRelease = ServerRelease.parseVersion(serverData.getVersionName());
        isAmberOrHigher = serverRelease.code() >= ServerRelease.AMBER.code();
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        loadFromNetwork(imageView, uri);
    }

    private void loadFromNetwork(ImageView imageView, String uri) {
        String path = "";
        if (isAmberOrHigher) {
            path = jsRestClient.generateThumbNailUri(uri);
        }
        ImageLoader.getInstance().displayImage(
                path, imageView, getDisplayImageOptions(),
                new ImageLoadingListener()
        );
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

    private static class ImageLoadingListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (view != null) {
                ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.MATRIX);
                ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.TOP_CROP);
            }
        }
    }

}
