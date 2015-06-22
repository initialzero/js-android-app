/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.retrofit.sdk.rest.service;

import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface AccountService {
    @Headers({"Accept: application/repository.folder+json", "Connection: close"})
    @GET("/resources")
    Observable<Response> authorize(@Header("Authorization") String authToken, @Header("Accept-Language") String locale);
    @Headers({"Accept: application/json", "Connection: close"})
    @GET("/serverInfo")
    Observable<ServerInfo> getServerInfo(@Header("Set-cookie") String cookie);
}
