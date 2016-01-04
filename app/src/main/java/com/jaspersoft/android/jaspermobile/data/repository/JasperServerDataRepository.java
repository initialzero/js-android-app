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

import com.jaspersoft.android.jaspermobile.data.cache.ServerCache;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.CloudServerDataSource;
import com.jaspersoft.android.jaspermobile.data.repository.datasource.DiskServerDataSource;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.ServerValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Implementation of repository pattern responsible CRUD operations around server meta data.
 * Corresponding implementation combines two different sources {@link DiskServerDataSource} and {@link CloudServerDataSource}.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class JasperServerDataRepository implements JasperServerRepository {

    /**
     * Injected by {@link ProfileModule#providesProfileRepository(ProfileDataRepository)}
     */
    private final ServerCache mServerCache;
    private final ServerValidator mServerValidator;

    @Inject
    public JasperServerDataRepository(ServerCache serverCache, ServerValidator serverValidator) {
        mServerCache = serverCache;
        mServerValidator = serverValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> saveServer(final Profile profile, final String serverUrl) {
        Observable<JasperServer> validateAction = mServerValidator.validate(serverUrl);
        return validateAction
                .doOnNext(new Action1<JasperServer>() {
                    @Override
                    public void call(JasperServer server) {
                        mServerCache.put(profile, server);
                    }
                }).map(new Func1<JasperServer, Profile>() {
                    @Override
                    public Profile call(JasperServer server) {
                        return profile;
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<JasperServer> getServer(final Profile profile) {
        throw new UnsupportedOperationException("Not yet implemented");
//        return Observable.defer(new Func0<Observable<JasperServer>>() {
//            @Override
//            public Observable<JasperServer> call() {
//                ServerDataSource source = mDataSourceFactory.createDataSource(profile);
//                try {
//                    JasperServer server = source.getServer(profile);
//                    return Observable.just(server);
//                } catch (RestStatusException e) {
//                   return Observable.error(e);
//                }
//            }
//        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Boolean> updateServer(final Profile profile) {
        throw new UnsupportedOperationException("Not yet implemented");
//        final ServerDataSource diskSource = mDataSourceFactory.createDiskDataSource();
//        final ServerDataSource cloudSource = mDataSourceFactory.createCloudDataSource();
//        return Observable.defer(new Func0<Observable<Boolean>>() {
//            @Override
//            public Observable<Boolean> call() {
//                try {
//                    JasperServer cachedServer = diskSource.getServer(profile);
//                    JasperServer networkServer = cloudSource.getServer(profile);
//                    boolean needUpdate = !cachedServer.equals(networkServer);
//                    if (needUpdate) {
//                        diskSource.saveServer(profile, networkServer);
//                    }
//                    return Observable.just(needUpdate);
//                } catch (RestStatusException e) {
//                   return Observable.error(e);
//                }
//            }
//        });
    }
}
