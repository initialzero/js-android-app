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

import com.jaspersoft.android.jaspermobile.data.cache.AccountProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.PreferencesActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;

/**
 * Implementation of repo pattern for {@link Profile}
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class ProfileDataRepository implements ProfileRepository {

    /**
     *  Injected by {@link ProfileModule#providesProfileAccountCache(AccountProfileCache)}}
     */
    private final ProfileCache mProfileCache;
    /**
     *  Injected by {@link ProfileModule#providesPreferencesProfileCache(PreferencesActiveProfileCache)}}
     */
    private final ActiveProfileCache mProfileActiveCache;

    @Inject
    public ProfileDataRepository(ProfileCache accountProfileCache,
                                 ActiveProfileCache preferencesProfileCache) {
        mProfileCache = accountProfileCache;
        mProfileActiveCache = preferencesProfileCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Void> saveProfile(final Profile profile) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                boolean containsProfile = mProfileCache.hasProfile(profile);
                if (containsProfile) {
                    return Observable.error(new FailedToSaveProfile(profile));
                } else {
                    boolean isSaved = mProfileCache.put(profile);
                    if (!isSaved) {
                        return Observable.error(new FailedToSaveProfile(profile));
                    }
                }
                return Observable.just(null);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Void> activate(final Profile profile) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                mProfileActiveCache.put(profile);
                return Observable.just(null);
            }
        });
    }
}
