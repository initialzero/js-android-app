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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;

/**
 * Stores credentials data inside {@link AccountManager} on the basis of passed {@link Profile}
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class AccountCredentialsCache implements CredentialsCache {
    public static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    public static final String USERNAME_KEY = "USERNAME_KEY";

    private final AccountManager mAccountManager;
    private final PasswordManager mPasswordManger;
    private final AccountDataMapper mAccountDataMapper;

    @Inject
    public AccountCredentialsCache(AccountManager accountManager,
                                   PasswordManager passwordManager,
                                   AccountDataMapper accountDataMapper) {
        mAccountManager = accountManager;
        mPasswordManger = passwordManager;
        mAccountDataMapper = accountDataMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<AppCredentials> putAsObservable(final Profile profile, final AppCredentials credentials)  {
        return Observable.defer(new Func0<Observable<AppCredentials>>() {
            @Override
            public Observable<AppCredentials> call() {
                Account account = mAccountDataMapper.transform(profile);

                String encryptedPassword = null;
                try {
                    encryptedPassword = mPasswordManger.encrypt(credentials.getPassword());
                } catch (PasswordManager.EncryptionException e) {
                    return Observable.error(e);
                }
                mAccountManager.setPassword(account, encryptedPassword);

                mAccountManager.setUserData(account, ORGANIZATION_KEY, credentials.getOrganization());
                mAccountManager.setUserData(account, USERNAME_KEY, credentials.getUsername());

                return Observable.just(credentials);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<AppCredentials> getAsObservable(final Profile profile) {
        return Observable.defer(new Func0<Observable<AppCredentials>>() {
            @Override
            public Observable<AppCredentials> call() {
                Account account = mAccountDataMapper.transform(profile);

                String password = mAccountManager.getPassword(account);
                String decryptedPassword = null;
                try {
                    decryptedPassword = mPasswordManger.decrypt(password);
                } catch (PasswordManager.DecryptionException e) {
                    return Observable.error(e);
                }

                AppCredentials appCredentials = buildCredentials(account, decryptedPassword);

                return Observable.just(appCredentials);
            }
        });
    }

    @NonNull
    @Override
    public AppCredentials get(Profile profile) {
        Account account = mAccountDataMapper.transform(profile);
        String password = mAccountManager.getPassword(account);
        String decryptedPassword = null;
        try {
            decryptedPassword = mPasswordManger.decrypt(password);
        } catch (PasswordManager.DecryptionException e) {
            decryptedPassword = "broken";
        }
        return buildCredentials(account, decryptedPassword);
    }

    private AppCredentials buildCredentials(Account account, String decryptedPassword) {
        String username = mAccountManager.getUserData(account, USERNAME_KEY);
        String organization = mAccountManager.getUserData(account, ORGANIZATION_KEY);

        AppCredentials.Builder credentialsBuilder = AppCredentials.builder();
        credentialsBuilder.setPassword(decryptedPassword);
        credentialsBuilder.setUsername(username);
        credentialsBuilder.setOrganization(organization);
        return credentialsBuilder.create();
    }
}
