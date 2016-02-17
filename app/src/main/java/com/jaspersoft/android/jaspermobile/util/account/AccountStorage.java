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


package com.jaspersoft.android.jaspermobile.util.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Pair;

import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.orhanobut.hawk.Storage;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.2.2
 */
public class AccountStorage implements Storage {
    public static final String KEY = "PASSWORD_KEY";

    private final AccountManager mAccountManager;
    private final AccountDataMapper mAccountDataMapper;
    private final ActiveProfileCache mActiveProfileCache;

    @Inject
    public AccountStorage(
            AccountManager accountManager,
            AccountDataMapper accountDataMapper,
            ActiveProfileCache activeProfileCache
    ) {
        mAccountManager = accountManager;
        mAccountDataMapper = accountDataMapper;
        mActiveProfileCache = activeProfileCache;
    }

    @Override
    public <T> boolean put(String key, T value) {
        if (containsPasswordKey(key)) {
            Account account = extractAccountFromPasswordKey(key);
            setAccountPassword(account, value);
        } else {
            return setUserData(key, value);
        }
        return true;
    }

    @Override
    public boolean put(List<Pair<String, ?>> items) {
        for (Pair<String, ?> item : items) {
            put(item.first, String.valueOf(item.second));
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (containsPasswordKey(key)) {
            Account account = extractAccountFromPasswordKey(key);
            return (T) getAccountPassword(account);
        } else {
            return (T) getUserData(key);
        }
    }

    @Override
    public boolean remove(String key) {
        if (containsPasswordKey(key)) {
            Account account = extractAccountFromPasswordKey(key);
            setAccountPassword(account, null);
        } else {
            return setUserData(key, null);
        }
        return true;
    }

    @Override
    public boolean remove(String... keys) {
        for (String key : keys) {
            remove(key);
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
        if (containsPasswordKey(key)) {
            Account account = extractAccountFromPasswordKey(key);
            return getAccountPassword(account) != null;
        }
        return getUserData(key) != null;
    }

    private boolean containsPasswordKey(String key) {
        return key.contains(AccountStorage.KEY);
    }

    private Account extractAccountFromPasswordKey(String key) {
        String profileKey = key.replace(AccountStorage.KEY, "");
        Profile profile = Profile.create(profileKey);
        return mAccountDataMapper.transform(profile);
    }

    private String getAccountPassword(Account account) {
        return mAccountManager.getPassword(account);
    }

    private <T> void setAccountPassword(Account account, T value) {
        mAccountManager.setPassword(account, String.valueOf(value));
    }

    private Account getActiveAccount() {
        Profile profile = mActiveProfileCache.get();
        return mAccountDataMapper.transform(profile);
    }

    private String getUserData(String key) {
        return mAccountManager.getUserData(getActiveAccount(), key);
    }

    private boolean setUserData(String key, Object value) {
        mAccountManager.setUserData(
                getActiveAccount(),
                key,
                (value == null) ? null : String.valueOf(value)
        );
        return true;
    }
}
