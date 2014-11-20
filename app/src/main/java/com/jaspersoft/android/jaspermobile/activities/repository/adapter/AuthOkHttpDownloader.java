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

package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.squareup.picasso.OkHttpDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;

class AuthOkHttpDownloader extends OkHttpDownloader {

    private final JsServerProfile mProfile;

    public AuthOkHttpDownloader(Context context, JsServerProfile profile) {
        super(context);
        mProfile = profile;
    }

    @Override
    protected HttpURLConnection openConnection(Uri uri) throws IOException {
        HttpURLConnection connection = super.openConnection(uri);

        String authorisation = mProfile.getUsernameWithOrgId() + ":" + mProfile.getPassword();
        String encodedAuthorisation = "Basic " + Base64.encodeToString(authorisation.getBytes(), Base64.NO_WRAP);
        connection.addRequestProperty("Authorization", encodedAuthorisation);
        connection.addRequestProperty("Accept", "image/jpeg");

        return connection;
    }

}
