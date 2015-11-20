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
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class TokenCacheImpl implements TokenCache {
    private static final String JASPER_AUTH_TOKEN_TYPE = "FULL ACCESS";

    private final String mAccountType;
    private final AccountManager mAccountManager;

    @Inject
    public TokenCacheImpl(AccountManager accountManager,
                          @Named("accountType") String accountType) {
        mAccountManager = accountManager;
        mAccountType = accountType;
    }

    @Override
    public String get(Profile profile) {
        Account account = new Account(profile.getKey(), mAccountType);
        return mAccountManager.peekAuthToken(account, JASPER_AUTH_TOKEN_TYPE);
    }

    @Override
    public void put(Profile profile, String token) {
        Account account = new Account(profile.getKey(), mAccountType);
        mAccountManager.setAuthToken(account, JASPER_AUTH_TOKEN_TYPE, token);
    }

    @Override
    public boolean isCached(Profile profile) {
        return !TextUtils.isEmpty(get(profile));
    }
}
