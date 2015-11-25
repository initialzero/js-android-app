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

import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;
import com.jaspersoft.android.sdk.service.server.ServerInfoService;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ServiceRestFactory {
    private final String serverUrl;
    private final int mConnectionReadTimeOut;
    private final int mConnectionTimeOut;

    ServiceRestFactory(String serverUrl, int connectionReadTimeOut, int connectionTimeOut) {
        this.serverUrl = serverUrl;
        mConnectionReadTimeOut = connectionReadTimeOut;
        mConnectionTimeOut = connectionTimeOut;
    }

    public JrsAuthenticator authenticator() {
        return JrsAuthenticator.create(serverUrl);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ServerInfoService serverApi() {
        return ServerInfoService.create(serverUrl);
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

        public void connectionReadTimeOut(int connectionReadTimeOut) {
            mConnectionReadTimeOut = connectionReadTimeOut;
        }

        public void connectionTimeOut(int connectionTimeOut) {
            mConnectionTimeOut = connectionTimeOut;
        }

        public ServiceRestFactory create() {
            return new ServiceRestFactory(mServerUrl, mConnectionReadTimeOut, mConnectionTimeOut);
        }
    }
}
