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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.jaspersoft.android.jaspermobile.data.cache.SecureCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.account.AccountStorage;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    private final SecureCache mSecureCache;
    private final AccountDataMapper mAccountDataMapper;

    @Inject
    public AccountCredentialsCache(AccountManager accountManager,
                                   SecureCache secureCache,
                                   AccountDataMapper accountDataMapper) {
        mAccountManager = accountManager;
        mSecureCache = secureCache;
        mAccountDataMapper = accountDataMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppCredentials put(final Profile profile, final AppCredentials credentials)  {
        Account account = mAccountDataMapper.transform(profile);
        mSecureCache.put(AccountStorage.KEY + account.name, credentials.getPassword());
        mAccountManager.setUserData(account, ORGANIZATION_KEY, credentials.getOrganization());
        mAccountManager.setUserData(account, USERNAME_KEY, credentials.getUsername());
        return credentials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppCredentials get(final Profile profile) {
        Account account = mAccountDataMapper.transform(profile);
        String password = mSecureCache.get(AccountStorage.KEY + account.name);

        String username = mAccountManager.getUserData(account, USERNAME_KEY);
        String organization = mAccountManager.getUserData(account, ORGANIZATION_KEY);

        AppCredentials.Builder credentialsBuilder = AppCredentials.builder();
        credentialsBuilder.setPassword(password);
        credentialsBuilder.setUsername(username);
        credentialsBuilder.setOrganization(organization);

        return credentialsBuilder.create();
    }
}
