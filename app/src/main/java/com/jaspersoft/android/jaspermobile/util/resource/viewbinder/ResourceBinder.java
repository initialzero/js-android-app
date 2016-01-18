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

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.request.SpiceRequest;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public abstract class ResourceBinder {
    private static final String LOG_TAG = ResourceBinder.class.getSimpleName();
    private final Context mContext;

    public ResourceBinder(Context context) {
        mContext = context;

        Timber.tag(ResourceBinder.LOG_TAG);
    }

    public void bindView(ResourceView resourceView, JasperResource item) {
        unbindView(resourceView.getImageView());
        setIcon(resourceView.getImageView(), item);
        setTitle(resourceView, item);
        setSubtitle(resourceView, item);
        setActionResource(resourceView, item);
    }

    public abstract void setIcon(TopCropImageView imageView, JasperResource jasperResource);

    protected void setTitle (ResourceView resourceView, JasperResource item) {
        resourceView.setTitle(item.getLabel());
    }

    protected void setSubtitle (ResourceView resourceView, JasperResource item) {
        resourceView.setSubTitle(item.getDescription());
    }

    protected void setActionResource(ResourceView resourceView, JasperResource item) {
        resourceView.setSecondaryAction(R.drawable.im_info);
    }

    protected Context getContext() {
        return mContext;
    }

    private void unbindView(TopCropImageView imageView){
        Object fileTypeRequest = imageView.getTag();
        if (fileTypeRequest != null && fileTypeRequest instanceof SpiceRequest) {
            ((RoboSpiceActivity) mContext).getSpiceManager().cancel((SpiceRequest) fileTypeRequest);
        }

        ImageLoader.getInstance().cancelDisplayTask(imageView);
    }
}
