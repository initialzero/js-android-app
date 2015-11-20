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

package com.jaspersoft.android.jaspermobile.data.repository.datasource;

import com.jaspersoft.android.jaspermobile.data.cache.JasperServerCache;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ServerDataSource {
    JasperServer getServer(Profile profile) throws RestStatusException;
    void saveServer(Profile profile, JasperServer server) throws RestStatusException;

    @Singleton
    class Factory {
        private final ServerApi.Factory mServerApiFactory;
        private final JasperServerCache mServerCache;

        @Inject
        public Factory(ServerApi.Factory serverApiFactory, JasperServerCache serverCache) {
            mServerApiFactory = serverApiFactory;
            mServerCache = serverCache;
        }

        public ServerDataSource createDataSource(Profile profile) {
            if (mServerCache.hasServer(profile)) {
                return createDiskDataSource();
            }
            return createCloudDataSource();
        }

        public ServerDataSource createCloudDataSource() {
            return new CloudServerDataSource(mServerApiFactory, mServerCache);
        }

        public ServerDataSource createDiskDataSource() {
           return new DiskServerDataSource(mServerCache);
        }
    }
}
