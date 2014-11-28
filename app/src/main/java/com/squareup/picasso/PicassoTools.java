/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.squareup.picasso;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class PicassoTools {
    private static final String TAG = PicassoTools.class.getSimpleName();
    private static final String PICASSO_CACHE = "picasso-cache";

    public static void clearCache(Picasso picasso) {
        File cache = new File(picasso.context.getApplicationContext().getCacheDir(), PICASSO_CACHE);
        try {
            FileUtils.cleanDirectory(cache);
        } catch (IOException e) {
            Log.w(TAG, "Failed to remove cache directory", e);
        }
        picasso.cache.clear();
    }
}
