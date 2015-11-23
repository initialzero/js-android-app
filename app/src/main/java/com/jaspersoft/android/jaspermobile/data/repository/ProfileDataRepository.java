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

import com.jaspersoft.android.jaspermobile.data.cache.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Implementation of repo pattern for {@link Profile}
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class ProfileDataRepository implements ProfileRepository {
    private final ProfileCache mProfileCache;
    private final ProfileCache mProfileActiveCache;

    @Inject
    public ProfileDataRepository(@Named("profileAccountCache") ProfileCache accountProfileCache,
                                 @Named("profilePreferencesCache") ProfileCache preferencesProfileCache) {
        mProfileCache = accountProfileCache;
        mProfileActiveCache = preferencesProfileCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProfile(Profile profile) throws FailedToSaveProfile {
        boolean isSaved = (!mProfileCache.hasProfile(profile) && mProfileCache.put(profile));
        if (!isSaved) {
            throw new FailedToSaveProfile(profile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(Profile profile) {
        mProfileActiveCache.put(profile);
    }
}
