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
import android.content.Context;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CredentialsCacheImpl implements CredentialsCache {
    public static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    public static final String USERNAME_KEY = "USERNAME_KEY";

    private final Context mContext;
    private final PasswordManager mPasswordManger;
    private final String mAccountType;

    @Inject
    public CredentialsCacheImpl(Context context,
                                PasswordManager passwordManager,
                                @Named("accountType") String accountType) {
        mContext = context;
        mPasswordManger = passwordManager;
        mAccountType = accountType;
    }

    @Override
    public boolean put(Profile profile, BaseCredentials credentials) {
        AccountManager accountManager = AccountManager.get(mContext);
        Account account = new Account(profile.getKey(), mAccountType);

        try {
            String encryptedPassword = mPasswordManger.encrypt(credentials.getPassword());
            accountManager.setPassword(account, encryptedPassword);
        } catch (PasswordManager.EncryptionError encryptionError) {
            return false;
        }

        accountManager.setUserData(account, ORGANIZATION_KEY, credentials.getOrganization());
        accountManager.setUserData(account, USERNAME_KEY, credentials.getUsername());

        return true;
    }
}
