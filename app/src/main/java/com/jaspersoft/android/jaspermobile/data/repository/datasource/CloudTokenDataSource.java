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

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class CloudTokenDataSource implements TokenDataSource {
    private final TokenCache mTokenCache;
    private final JasperServer mServer;
    private final Profile mProfile;
    private final BaseCredentials mCredentials;
    private final Authenticator.Factory mAuthFactory;

    public CloudTokenDataSource(TokenCache tokenCache,
                                Authenticator.Factory authFactory,
                                Profile profile,
                                JasperServer server,
                                BaseCredentials credentials) {
        mAuthFactory = authFactory;
        mTokenCache = tokenCache;
        mProfile = profile;
        mServer = server;
        mCredentials = credentials;
    }

    @Override
    public String retrieveToken() throws RestStatusException {
        Authenticator authenticator = mAuthFactory.create(mServer.getBaseUrl());
        String token = authenticator.authenticate(mCredentials);
        mTokenCache.put(mProfile, token);
        return token;
    }
}
