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

package com.jaspersoft.android.jaspermobile.data.repository.profile;

import com.jaspersoft.android.jaspermobile.data.cache.profile.AccountProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.PreferencesActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.CacheModule;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Implementation of repo pattern for {@link Profile}
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class ProfileDataRepository implements ProfileRepository {

    /**
     * Injected by {@link CacheModule#providesProfileAccountCache(AccountProfileCache)}}
     */
    private final ProfileCache mAccountCache;
    /**
     * Injected by {@link CacheModule#providesPreferencesProfileCache(PreferencesActiveProfileCache)}}
     */
    private final ActiveProfileCache mPrefActiveCache;

    @Inject
    public ProfileDataRepository(ProfileCache accountProfileCache,
                                 ActiveProfileCache preferencesProfileCache) {
        mAccountCache = accountProfileCache;
        mPrefActiveCache = preferencesProfileCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> saveProfile(final Profile profile) {
        return mAccountCache.put(profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> activate(final Profile profile) {
        return mPrefActiveCache.put(profile);
    }
}
