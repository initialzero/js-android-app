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
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class ProfileCacheImpl implements ProfileCache {
    private static final String ALIAS_KEY = "ALIAS_KEY";

    private final Context mContext;
    private final String mAccountType;

    @Inject
    public ProfileCacheImpl(Context context, @Named("accountType") String accountType) {
        mContext = context;
        mAccountType = accountType;
    }

    @Override
    public boolean put(Profile profile) {
        AccountManager accountManager = AccountManager.get(mContext);
        Account accountProfile = new Account(profile.getKey(), mAccountType);
        Bundle userData = new Bundle();
        userData.putString(ALIAS_KEY, profile.getKey());
        return accountManager.addAccountExplicitly(accountProfile, null, userData);
    }

    @Override
    public boolean hasProfile(Profile profile) {
        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(mAccountType);
        Account accountProfile = new Account(profile.getKey(), mAccountType);
        Set<Account> accountsSet = new HashSet<>(Arrays.asList(accounts));
        return accountsSet.contains(accountProfile);
    }
}
