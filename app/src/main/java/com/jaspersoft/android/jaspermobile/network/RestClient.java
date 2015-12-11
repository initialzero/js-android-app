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

import com.jaspersoft.android.sdk.network.ServerRestApi;
import com.jaspersoft.android.sdk.service.auth.AuthenticationService;
import com.jaspersoft.android.sdk.service.server.ServerInfoService;

import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class RestClient {
    private final String mServerUrl;
    private final int mReadTimeOut;
    private final int mConnectionTimeOut;

    private AuthenticationService mAuthenticator;
    private ServerInfoService mInfoService;

    RestClient(String serverUrl, int readTimeOut, int connectionTimeOut) {
        mServerUrl = serverUrl;
        mReadTimeOut = readTimeOut;
        mConnectionTimeOut = connectionTimeOut;
    }

    public AuthenticationService authApi() {
        if (mAuthenticator == null) {
            com.jaspersoft.android.sdk.service.RestClient client =
                    com.jaspersoft.android.sdk.service.RestClient.builder()
                    .serverUrl(mServerUrl)
                    .connectionTimeOut(mConnectionTimeOut, TimeUnit.MILLISECONDS)
                    .readTimeOut(mReadTimeOut, TimeUnit.MILLISECONDS)
                    .create();

            mAuthenticator = AuthenticationService.create(client);
        }
        return mAuthenticator;
    }

    public ServerInfoService infoApi() {
        if (mInfoService == null) {
            ServerRestApi restApi = new ServerRestApi.Builder()
                    .connectionTimeOut(mConnectionTimeOut, TimeUnit.MILLISECONDS)
                    .readTimeout(mReadTimeOut, TimeUnit.MILLISECONDS)
                    .baseUrl(mServerUrl)
                    .build();
            mInfoService = ServerInfoService.create(restApi);
        }
        return mInfoService;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String mServerUrl;
        private int mConnectionReadTimeOut;
        private int mConnectionTimeOut;

        private Builder() {}

        public Builder serverUrl(String serverUrl) {
            mServerUrl = serverUrl;
            return this;
        }

        public Builder connectionReadTimeOut(int connectionReadTimeOut) {
            mConnectionReadTimeOut = connectionReadTimeOut;
            return this;
        }

        public Builder connectionTimeOut(int connectionTimeOut) {
            mConnectionTimeOut = connectionTimeOut;
            return this;
        }

        public RestClient create() {
            return new RestClient(mServerUrl, mConnectionReadTimeOut, mConnectionTimeOut);
        }
    }
}
