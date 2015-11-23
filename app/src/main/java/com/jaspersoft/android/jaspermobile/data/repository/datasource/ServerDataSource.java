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
 * Abstraction around different data sources application supports.
 * Following interface implemented by {@link DiskServerDataSource} and {@link CloudServerDataSource}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface ServerDataSource {
    /**
     * Retrieves server from either disk or network.
     *
     * @param profile the target profile we use to associate with credentials
     * @return {@link JasperServer} abstraction that encompass additional server metadata
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    JasperServer getServer(Profile profile) throws RestStatusException;

    /**
     * Fetches latest available server data on the basis of corresponding http url.
     *
     * @param baseUrl represents network address of JasperServer
     * @return {@link JasperServer} abstraction that encompass additional server metadata
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    JasperServer getServer(String baseUrl) throws RestStatusException;

    /**
     * Updates cached server data.
     *
     * @param profile the target profile we use to associate with credentials
     */
    void saveServer(Profile profile, JasperServer server);

    @Singleton
    class Factory {
        private final ServerApi.Factory mServerApiFactory;
        private final JasperServerCache mServerCache;

        @Inject
        public Factory(ServerApi.Factory serverApiFactory, JasperServerCache serverCache) {
            mServerApiFactory = serverApiFactory;
            mServerCache = serverCache;
        }

        /**
         * Choose strategy for accessing server data on the condition of server availability in cache.
         *
         * @param profile the target profile we use to associate with credentials
         * @return If exists in cache returns {@link DiskServerDataSource}, else {@link CloudServerDataSource}
         */
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
