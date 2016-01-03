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

import com.jaspersoft.android.jaspermobile.data.cache.ServerCache;
import com.jaspersoft.android.jaspermobile.data.cache.AccountServerCache;
import com.jaspersoft.android.jaspermobile.data.network.ServerApiFactory;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.jaspermobile.internal.di.modules.AppModule;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

/**
 * This implementation connects to network to retrieve latest server metadata.
 *
 * @author Tom Koptel
 * @since 2.3
 */
public final class CloudServerDataSource implements ServerDataSource {

    /**
     * Injected by {@link AppModule#providesServerInfoFactory(ServerApiFactory)}}
     */
    private final ServerApi.Factory mServerApiFactory;
    /**
     * Injected by {@link ProfileModule#providesJasperSeverCache(AccountServerCache)}}
     */
    private final ServerCache mServerCache;

    public CloudServerDataSource(ServerApi.Factory serverApiFactory, ServerCache serverCache) {
        mServerApiFactory = serverApiFactory;
        mServerCache = serverCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer getServer(Profile profile) throws RestStatusException {
        JasperServer server = mServerCache.get(profile);
        String baseUrl = server.getBaseUrl();
        return getServer(baseUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer getServer(String baseUrl) throws RestStatusException {
        throw new UnsupportedOperationException("Not yet implemented");
//        ServerApi serverApi = mServerApiFactory.create(baseUrl);
//        return serverApi.requestServer();
    }

    /**
     * We do not support persistence of server on Jasper Server side. It makes no sense for
     * cloud data source.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void saveServer(Profile profile, JasperServer server) {
        throw new UnsupportedOperationException("There is no way we can save server across network call");
    }
}