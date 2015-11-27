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

package com.jaspersoft.android.jaspermobile.util.account;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

/**
 * @author Tom Koptel
 * @since 2.2.2
 */
public class ActiveAccountCache {
    public static final String KEY = "ACCOUNT_NAME_KEY";
    private static final String PREF_NAME = "JasperAccountManager";

    private final SharedPreferences mPreference;
    private final String mAccountType;

    ActiveAccountCache(SharedPreferences preference, String accountType) {
        mPreference = preference;
        mAccountType = accountType;
    }

    @NonNull
    public static ActiveAccountCache create(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return new ActiveAccountCache(pref, JasperSettings.JASPER_ACCOUNT_TYPE);
    }

    public void put(Account account) {
        mPreference.edit().putString(KEY, account.name).apply();
    }

    @Nullable
    public Account get() {
        String name = mPreference.getString(KEY, null);
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        return new Account(name, mAccountType);
    }

    public void clear() {
        mPreference.edit().remove(KEY).apply();
    }
}
