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

package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.repository.datasource.ServerDataSource;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class JasperServerDataRepository implements JasperServerRepository {
    private final ServerDataSource.Factory mDataSourceFactory;

    @Inject
    public JasperServerDataRepository(ServerDataSource.Factory dataSourceFactory) {
        mDataSourceFactory = dataSourceFactory;
    }

    @Override
    public void saveServer(Profile profile, JasperServer jasperServer) {
        ServerDataSource source = mDataSourceFactory.createDiskDataSource();
        try {
            source.saveServer(profile, jasperServer);
        } catch (RestStatusException e) {
            /**
             * This exception literary should not happen at all as soon as disk data source do no network calls
             */
            throw new IllegalStateException("Boom! Disk data source threw REST exception!");
        }
    }

    @Override
    public JasperServer loadServer(String baseUrl) throws RestStatusException {
        ServerDataSource cloudSource = mDataSourceFactory.createCloudDataSource();
        return cloudSource.fetchServerData(baseUrl);
    }

    @Override
    public JasperServer getServer(Profile profile) throws RestStatusException {
        ServerDataSource source = mDataSourceFactory.createDataSource(profile);
        return source.getServer(profile);
    }

    @Override
    public boolean updateServer(Profile profile) throws RestStatusException {
        ServerDataSource diskSource = mDataSourceFactory.createDiskDataSource();
        ServerDataSource cloudSource = mDataSourceFactory.createCloudDataSource();
        JasperServer cachedServer = diskSource.getServer(profile);
        JasperServer networkServer = cloudSource.getServer(profile);

        boolean needUpdate = !cachedServer.equals(networkServer);
        if (needUpdate) {
            diskSource.saveServer(profile, networkServer);
        }
        return needUpdate;
    }
}
