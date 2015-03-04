/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.network;

import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class TokenImageDownloader extends BaseImageDownloader {
    private static final String COOKIE_HEADER = "Cookie";
    private static final String ACCEPT_HEADER = "Accept";

    private final JasperAccountManager jasperAccountManager;

    public TokenImageDownloader(Context context) {
        super(context);
        jasperAccountManager = JasperAccountManager.get(context);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = super.createConnection(url, extra);
        String token = jasperAccountManager.getActiveAuthToken();
        conn.setRequestProperty(COOKIE_HEADER, token);
        conn.setRequestProperty(ACCEPT_HEADER, "image/jpeg");
        return conn;
    }
}
