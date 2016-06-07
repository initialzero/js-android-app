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

package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.squareup.okhttp.HttpUrl;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ThumbNailGenerator {
    private final JasperServer mAppServer;

    @Inject
    public ThumbNailGenerator(JasperServer appServer) {
        mAppServer = appServer;
    }

    @NonNull
    public String generate(@NonNull String resourceUri) {
        String version = mAppServer.getVersion();
        ServerVersion serverVersion = ServerVersion.valueOf(version);
        if (serverVersion.greaterThanOrEquals(ServerVersion.v6)) {
            HttpUrl endpoint = HttpUrl.parse(mAppServer.getBaseUrl())
                    .newBuilder()
                    .addPathSegment("rest_v2")
                    .addPathSegment("thumbnails")
                    .build();
            HttpUrl resourceEndpoint = HttpUrl.parse(endpoint.toString() + resourceUri)
                    .newBuilder()
                    .addQueryParameter("defaultAllowed", "false")
                    .build();
            return resourceEndpoint.toString();
        }
        return "";
    }
}
