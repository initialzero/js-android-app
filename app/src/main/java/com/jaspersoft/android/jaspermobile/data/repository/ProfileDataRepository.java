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
import com.jaspersoft.android.jaspermobile.data.validator.ProfileValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.validator.ProfileValidator;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

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
     * Injected by {@link ProfileModule#providesProfileAccountCache(AccountProfileCache)}}
     */
    private final ProfileCache mProfileCache;
    /**
     * Injected by {@link ProfileModule#providesPreferencesProfileCache(PreferencesActiveProfileCache)}}
     */
    private final ActiveProfileCache mProfileActiveCache;
    /**
     * Injected by {@link ProfileModule#provideProfileValidator(ProfileValidatorImpl)}
     */
    private final ProfileValidator mProfileValidator;

    @Inject
    public ProfileDataRepository(ProfileCache accountProfileCache,
                                 ActiveProfileCache preferencesProfileCache,
                                 ProfileValidator profileValidator) {
        mProfileCache = accountProfileCache;
        mProfileActiveCache = preferencesProfileCache;
        mProfileValidator = profileValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> saveProfile(final Profile profile) {
        Observable<Profile> validateAction = mProfileValidator.validate(profile);
        Observable<Profile> saveAction = mProfileCache.put(profile);
        return validateAction.concatWith(saveAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> activate(final Profile profile) {
        return mProfileActiveCache.put(profile);
    }
}
