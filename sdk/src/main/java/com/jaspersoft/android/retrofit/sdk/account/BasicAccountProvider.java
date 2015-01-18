/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.retrofit.sdk.account;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

/**
 * Basic implementation of {@link com.jaspersoft.android.retrofit.sdk.account.AccountProvider} interface.
 * Mostly util class which stores {@link android.accounts.Account} object name property using
 * {@link android.content.SharedPreferences}.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccountProvider implements AccountProvider {
    private static final String PREF_NAME = BasicAccountProvider.class.getSimpleName();
    private static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME_KEY";

    private final SharedPreferences mPreference;

    public static BasicAccountProvider get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context should not be 'null'");
        }
        return new BasicAccountProvider(context);
    }

    private BasicAccountProvider(Context context) {
        mPreference = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }

    @Override
    public Account getAccount() {
        String accountName = mPreference.getString(ACCOUNT_NAME_KEY, "");
        if (TextUtils.isEmpty(accountName)) {
            throw new IllegalStateException("You need call 'putAccount()' before calling this.");
        }
        return new Account(accountName, JasperSettings.JASPER_ACCOUNT_TYPE);
    }

    @Override
    public void putAccount(Account account) {
        mPreference.edit().putString(ACCOUNT_NAME_KEY, account.name).apply();
    }

    public void clear() {
        mPreference.edit().putString(ACCOUNT_NAME_KEY, "").apply();
    }
}
