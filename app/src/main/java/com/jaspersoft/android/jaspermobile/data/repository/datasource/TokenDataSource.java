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

import com.jaspersoft.android.jaspermobile.data.cache.TokenCache;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Abstraction around token data source
 * <br/>
 * Implemented by {@link DiskTokenDataSource} and {@link CloudTokenDataSource}
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface TokenDataSource {
    /**
     * Retrieves token from data source.
     *
     * @return token or cookie received from Jasper Server
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    String retrieveToken() throws RestStatusException;

    @Singleton
    class Factory {
        private final Authenticator.Factory mAuthFactory;
        private final TokenCache mTokenCache;

        @Inject
        public Factory(Authenticator.Factory authFactory, TokenCache tokenCache) {
            mAuthFactory = authFactory;
            mTokenCache = tokenCache;
        }

        /**
         * Searches for token in cache, then returns either {@link DiskTokenDataSource} or {@link CloudTokenDataSource}
         *
         * @param profile we use for checkup in cache
         * @param server we use to build {@link CloudTokenDataSource}
         * @param credentials we use to build {@link CloudTokenDataSource}
         * @return implementation of data source
         */
        public TokenDataSource create(Profile profile, JasperServer server, BaseCredentials credentials) {
            boolean hasToken = mTokenCache.isCached(profile);
            if (hasToken) {
                return new DiskTokenDataSource(profile, mTokenCache);
            }
            return new CloudTokenDataSource(mTokenCache, mAuthFactory, profile, server, credentials);
        }
    }
}
