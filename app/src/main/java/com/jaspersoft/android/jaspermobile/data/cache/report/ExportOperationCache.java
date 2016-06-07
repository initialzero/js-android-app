/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.net.Uri;
import android.os.AsyncTask;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ExportOperationCache {
    private final Map<Uri, AsyncTask<?, ?, ?>> mCache = new HashMap<>();

    @Inject
    public ExportOperationCache() {
    }

    public void add(Uri key, AsyncTask<?, ?, ?> operation) {
        mCache.put(key, operation);
    }

    public void remove(Uri key) {
        mCache.remove(key);
    }

    public AsyncTask<?, ?, ?> get(Uri current) {
        return mCache.get(current);
    }
}
