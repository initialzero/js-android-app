/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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
import com.jaspersoft.android.jaspermobile.data.validator.CredentialsValidatorImpl;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToRetrieveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.internal.di.modules.CredentialsModule;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * Implements repository pattern to control CRUD operations around credentials.
 * Current implementations build around {@link CredentialsCache} abstraction.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class CredentialsDataRepository implements CredentialsRepository {
    /**
     * Injected by {@link CredentialsModule#provideCredentialsCache(Context, AccountManager, AccountDataMapper)}}
     */
    private final CredentialsCache mCredentialsCache;
    /**
     * Injected by {@link CredentialsModule#providesCredentialsValidator(CredentialsValidatorImpl)}
     */
    private final CredentialsValidator mCredentialsValidator;

    @Inject
    public CredentialsDataRepository(CredentialsCache credentialsCache, CredentialsValidator credentialsValidator) {
        mCredentialsCache = credentialsCache;
        mCredentialsValidator = credentialsValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> saveCredentials(final Profile profile, final AppCredentials credentials) {
        Observable<AppCredentials> validateAction = mCredentialsValidator.validate(credentials);
        Observable<AppCredentials> saveAction = mCredentialsCache.put(profile, credentials);
        return validateAction.concatWith(saveAction)
                .flatMap(new Func1<AppCredentials, Observable<Profile>>() {
                    @Override
                    public Observable<Profile> call(AppCredentials appCredentials) {
                        return Observable.just(profile);
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<Profile>>() {
                    @Override
                    public Observable<Profile> call(Throwable throwable) {
                        if (throwable instanceof PasswordManager.EncryptionException) {
                            return Observable.error(new FailedToSaveCredentials(credentials));
                        }
                        return Observable.error(throwable);
                    }
                });
    }

    /**
     *
     * if (throwable instanceof PasswordManager.EncryptionException) {
     return Observable.error(new FailedToSaveCredentials(credentials));
     }
     return Observable.error(throwable);
     *
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<AppCredentials> getCredentials(final Profile profile) {
        return mCredentialsCache.get(profile)
                .onErrorResumeNext(new Func1<Throwable, Observable<AppCredentials>>() {
                    @Override
                    public Observable<AppCredentials> call(Throwable throwable) {
                        if (throwable instanceof PasswordManager.DecryptionException) {
                            return Observable.error(new FailedToRetrieveCredentials(profile, throwable));
                        }
                        return Observable.error(throwable);
                    }
                });
    }
}
