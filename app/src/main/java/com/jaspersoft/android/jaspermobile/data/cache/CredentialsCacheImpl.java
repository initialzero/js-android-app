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

package com.jaspersoft.android.jaspermobile.data.cache;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CredentialsCacheImpl implements CredentialsCache {
    public static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    public static final String USERNAME_KEY = "USERNAME_KEY";

    private final AccountManager mAccountManager;
    private final PasswordManager mPasswordManger;
    private final AccountDataMapper mAccountDataMapper;

    @Inject
    public CredentialsCacheImpl(AccountManager accountManager,
                                PasswordManager passwordManager,
                                AccountDataMapper accountDataMapper) {
        mAccountManager = accountManager;
        mPasswordManger = passwordManager;
        mAccountDataMapper = accountDataMapper;
    }

    @Override
    public void put(Profile profile, BaseCredentials credentials) throws PasswordManager.EncryptionException {
        Account account = mAccountDataMapper.transform(profile);

        String encryptedPassword = mPasswordManger.encrypt(credentials.getPassword());
        mAccountManager.setPassword(account, encryptedPassword);

        mAccountManager.setUserData(account, ORGANIZATION_KEY, credentials.getOrganization());
        mAccountManager.setUserData(account, USERNAME_KEY, credentials.getUsername());
    }

    @Override
    public BaseCredentials get(Profile profile) throws PasswordManager.DecryptionException {
        Account account = mAccountDataMapper.transform(profile);

        String password = mAccountManager.getPassword(account);
        String decryptedPassword = mPasswordManger.decrypt(password);

        String username = mAccountManager.getUserData(account, USERNAME_KEY);
        String organization = mAccountManager.getUserData(account, ORGANIZATION_KEY);

        BaseCredentials.Builder credentialsBuilder = BaseCredentials.builder();
        credentialsBuilder.setPassword(decryptedPassword);
        credentialsBuilder.setUsername(username);
        credentialsBuilder.setOrganization(organization);
        return credentialsBuilder.create();
    }
}
