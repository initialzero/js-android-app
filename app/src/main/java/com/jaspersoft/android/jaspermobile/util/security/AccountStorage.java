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

package com.jaspersoft.android.jaspermobile.util.security;

import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Pair;

import com.jaspersoft.android.jaspermobile.util.account.ActiveAccountCache;
import com.orhanobut.hawk.Storage;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.2.2
 */
public class AccountStorage implements Storage {
    private final AccountManager mAccountManager;
    private final ActiveAccountCache mAccountCache;

    @VisibleForTesting
    AccountStorage(AccountManager accountManager, ActiveAccountCache accountCache) {
        mAccountManager = accountManager;
        mAccountCache = accountCache;
    }

    public static AccountStorage create(Context context) {
        ActiveAccountCache activeAccountCache = ActiveAccountCache.create(context);
        AccountManager accountManager = AccountManager.get(context);
        return new AccountStorage(accountManager, activeAccountCache);
    }

    @Override
    public <T> boolean put(String key, T value) {
        mAccountManager.setUserData(mAccountCache.get(), key, String.valueOf(value));
        return true;
    }

    @Override
    public boolean put(List<Pair<String, ?>> items) {
        for (Pair<String, ?> item : items) {
            mAccountManager.setUserData(mAccountCache.get(), item.first, String.valueOf(item.second));
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) mAccountManager.getUserData(mAccountCache.get(), key);
    }

    @Override
    public boolean remove(String key) {
        mAccountManager.setUserData(mAccountCache.get(), key, null);
        return true;
    }

    @Override
    public boolean remove(String... keys) {
        for (String key : keys) {
            mAccountManager.setUserData(mAccountCache.get(), key, null);
        }
        return true;
    }

    @Override
    public boolean clear() {
        // Not supported
        return false;
    }

    @Override
    public long count() {
        // Not supported
        return 0;
    }

    @Override
    public boolean contains(String key) {
        return mAccountManager.getUserData(mAccountCache.get(), key) != null;
    }
}
