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

import com.jaspersoft.android.jaspermobile.data.cache.CredentialsCache;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToRetrieveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implements repository pattern to control CRUD operations around credentials.
 * Current implementations build around {@link CredentialsCache} abstraction.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CredentialsDataRepository implements CredentialsRepository {
    private final CredentialsCache mCredentialsCache;

    @Inject
    public CredentialsDataRepository(CredentialsCache credentialsCache) {
        mCredentialsCache = credentialsCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCredentials(Profile profile, BaseCredentials credentials) throws FailedToSaveCredentials {
        try {
            mCredentialsCache.put(profile, credentials);
        } catch (PasswordManager.EncryptionException e) {
            throw new FailedToSaveCredentials(credentials);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseCredentials getCredentials(Profile profile) throws FailedToRetrieveCredentials {
        try {
            return mCredentialsCache.get(profile);
        } catch (PasswordManager.DecryptionException e) {
            throw new FailedToRetrieveCredentials(profile, e);
        }
    }
}
