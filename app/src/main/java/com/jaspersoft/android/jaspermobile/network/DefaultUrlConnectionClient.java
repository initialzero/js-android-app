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

package com.jaspersoft.android.jaspermobile.network;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DefaultUrlConnectionClient extends UrlConnectionClient {
    private final int mConnectionReadTimeOut;
    private final int mConnectionTimeOut;

    public DefaultUrlConnectionClient(Context context) {
        DefaultPrefHelper defaultPrefHelper = DefaultPrefHelper_.getInstance_(context);
        mConnectionReadTimeOut = defaultPrefHelper.getReadTimeoutValue();
        mConnectionTimeOut = defaultPrefHelper.getConnectTimeoutValue();
    }

    @Override
    protected HttpURLConnection openConnection(Request request) {
        HttpURLConnection connection = null;
        try {
            connection = super.openConnection(request);
            connection.setConnectTimeout(mConnectionTimeOut);
            connection.setReadTimeout(mConnectionReadTimeOut);
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return connection;
    }
}