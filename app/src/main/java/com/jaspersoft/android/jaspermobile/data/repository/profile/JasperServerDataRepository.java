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

package com.jaspersoft.android.jaspermobile.data.repository.profile;

import com.jaspersoft.android.jaspermobile.data.cache.profile.AccountJasperServerCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.JasperServerMapper;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.CacheModule;
import com.jaspersoft.android.sdk.network.AnonymousClient;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.info.ServerInfoService;

import java.net.CookieHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of repository pattern responsible CRUD operations around server meta data.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class JasperServerDataRepository implements JasperServerRepository {

    /**
     * Injected by {@link CacheModule#providesJasperSeverCache(AccountJasperServerCache)}}
     */
    private final JasperServerCache mJasperServerCache;
    private final JasperServerMapper mJasperServerMapper;
    private final Server.Builder mServerBuilder;

    @Inject
    public JasperServerDataRepository(JasperServerCache jasperServerCache,
                                      JasperServerMapper jasperServerMapper,
                                      Server.Builder serverBuilder) {
        mJasperServerCache = jasperServerCache;
        mJasperServerMapper = jasperServerMapper;
        mServerBuilder = serverBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer saveServer(final Profile profile, final JasperServer server) {
        mJasperServerCache.put(profile, server);
        return server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer getServer(final Profile profile) {
        return mJasperServerCache.get(profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JasperServer loadServer(final String serverUrl) throws Exception {
        Server server = mServerBuilder.withBaseUrl(serverUrl).build();
        AnonymousClient client = server.newClient()
                .withCookieHandler(CookieHandler.getDefault())
                .create();
        ServerInfoService infoService = ServerInfoService.newService(client);
        ServerInfo serverInfo = infoService.requestServerInfo();
        return mJasperServerMapper.toDomainModel(serverUrl, serverInfo);
    }
}
