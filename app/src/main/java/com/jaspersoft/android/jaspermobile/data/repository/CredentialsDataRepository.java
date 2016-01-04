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

import android.accounts.AccountManager;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.data.cache.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToRetrieveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;

/**
 * Implements repository pattern to control CRUD operations around credentials.
 * Current implementations build around {@link CredentialsCache} abstraction.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CredentialsDataRepository implements CredentialsRepository {
    /**
     * Injected by {@link ProfileModule#provideCredentialsCache(Context, AccountManager, AccountDataMapper)}}
     */
    private final CredentialsCache mCredentialsCache;

    @Inject
    public CredentialsDataRepository(CredentialsCache credentialsCache) {
        mCredentialsCache = credentialsCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Void> saveCredentials(final Profile profile, final AppCredentials credentials) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                try {
                    mCredentialsCache.put(profile, credentials);
                } catch (PasswordManager.EncryptionException e) {
                    return Observable.error(new FailedToSaveCredentials(credentials));
                }
                return Observable.just(null);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<AppCredentials> getCredentials(final Profile profile) {
        return Observable.defer(new Func0<Observable<AppCredentials>>() {
            @Override
            public Observable<AppCredentials> call() {
                try {
                    return Observable.just(mCredentialsCache.get(profile));
                } catch (PasswordManager.DecryptionException e) {
                    return Observable.error(new FailedToRetrieveCredentials(profile, e));
                }
            }
        });
    }
}
