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

import com.jaspersoft.android.jaspermobile.data.repository.datasource.CloudServerDataSource;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.DiskServerDataSource;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.ServerDataSource;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;

/**
 * Implementation of repository pattern responsible CRUD operations around server meta data.
 * Corresponding implementation combines two different sources {@link DiskServerDataSource} and {@link CloudServerDataSource}.
 *
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Void> saveServer(final Profile profile, final JasperServer jasperServer) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                ServerDataSource source = mDataSourceFactory.createDiskDataSource();
                source.saveServer(profile, jasperServer);
                return Observable.just(null);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<JasperServer> loadServer(final String baseUrl) {
        return Observable.defer(new Func0<Observable<JasperServer>>() {
            @Override
            public Observable<JasperServer> call() {
                ServerDataSource cloudSource = mDataSourceFactory.createCloudDataSource();
                try {
                    JasperServer server = cloudSource.getServer(baseUrl);
                    return Observable.just(server);
                } catch (RestStatusException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<JasperServer> getServer(final Profile profile)  {
        return Observable.defer(new Func0<Observable<JasperServer>>() {
            @Override
            public Observable<JasperServer> call() {
                ServerDataSource source = mDataSourceFactory.createDataSource(profile);
                try {
                    JasperServer server = source.getServer(profile);
                    return Observable.just(server);
                } catch (RestStatusException e) {
                   return Observable.error(e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Boolean> updateServer(final Profile profile){
        final ServerDataSource diskSource = mDataSourceFactory.createDiskDataSource();
        final ServerDataSource cloudSource = mDataSourceFactory.createCloudDataSource();
        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                try {
                    JasperServer cachedServer = diskSource.getServer(profile);
                    JasperServer networkServer = cloudSource.getServer(profile);
                    boolean needUpdate = !cachedServer.equals(networkServer);
                    if (needUpdate) {
                        diskSource.saveServer(profile, networkServer);
                    }
                    return Observable.just(needUpdate);
                } catch (RestStatusException e) {
                   return Observable.error(e);
                }
            }
        });
    }
}
