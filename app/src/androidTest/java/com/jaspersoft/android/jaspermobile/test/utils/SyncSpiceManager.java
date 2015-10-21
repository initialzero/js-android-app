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

package com.jaspersoft.android.jaspermobile.test.utils;

import android.util.Log;

import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SyncSpiceManager extends JsSpiceManager {
    @Override
    public <T> void execute(CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
        try {
            requestListener.onRequestSuccess(cachedSpiceRequest.loadDataFromNetwork());
        } catch (Exception e) {
            requestListener.onRequestFailure( new NetworkException("Exception occurred during invocation of web service.", e));
            Log.e(SyncSpiceManager.class.getSimpleName(), "SyncSpiceManager processed error: ", e);
        }
    }
}
