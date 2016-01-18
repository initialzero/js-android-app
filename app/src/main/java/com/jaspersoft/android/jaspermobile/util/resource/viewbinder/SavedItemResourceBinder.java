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
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.filtering.StorageResourceFilter;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;
import com.jaspersoft.android.jaspermobile.util.resource.SavedItemResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class SavedItemResourceBinder extends ResourceBinder {

    private static final Map<SavedItemResource.FileType, Integer> DRAWABLE_IDS_MAP = new EnumMap<SavedItemResource.FileType, Integer>(SavedItemResource.FileType.class);

    static {
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.HTML, R.drawable.ic_file_html);
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.PDF, R.drawable.ic_file_pdf);
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.XLS, R.drawable.ic_file_xls);
    }

    public SavedItemResourceBinder(Context context) {
        super(context);
    }

    @Override
    public void setIcon(TopCropImageView imageView, JasperResource jasperResource) {
        imageView.setScaleType(TopCropImageView.ScaleType.CENTER);
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);

        if (jasperResource.getResourceType() == JasperResourceType.saved_item) {
            SavedItemResource.FileType fileType = ((SavedItemResource) jasperResource).getFileType();
            int iconRes = DRAWABLE_IDS_MAP.get(fileType);
            imageView.setImageResource(iconRes);
        }
    }
}
