/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class FolderResourceBinder extends ResourceBinder {

    private static final String FIRST_INITIAL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String SECOND_INITIAL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final SimpleDateFormat[] serverDateFormats;

    public FolderResourceBinder(Context context) {
        super(context);

        Locale current = context.getResources().getConfiguration().locale;
        serverDateFormats = new SimpleDateFormat[]{new SimpleDateFormat(FIRST_INITIAL_DATE_FORMAT, current),
                new SimpleDateFormat(SECOND_INITIAL_DATE_FORMAT, current)};
    }

    @Override
    public void bindView(ResourceView resourceView, ResourceLookup item) {
        super.bindView(resourceView, item);
        resourceView.setTimeStamp(formatDateString(item.getCreationDate()));
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_XY);
        imageView.setBackgroundResource(R.drawable.bg_gradient_blue);
        imageView.setImageResource(R.drawable.placeholder_folder);
    }

    private String formatDateString(String updateDate) {
        if (updateDate == null) return "";

        try {
            Date dateValue = serverDateFormats[0].parse(updateDate);
            DateFormat dateFormat = DateFormat.getDateInstance();
            return dateFormat.format(dateValue);
        } catch (ParseException ex) {
            Timber.w("Wrong date format");
        }

        try {
            Date dateValue = serverDateFormats[1].parse(updateDate);
            DateFormat dateFormat = DateFormat.getDateInstance();
            return dateFormat.format(dateValue);
        } catch (ParseException ex) {
            Timber.w("Wrong date format");
        }

        return updateDate;
    }
}
