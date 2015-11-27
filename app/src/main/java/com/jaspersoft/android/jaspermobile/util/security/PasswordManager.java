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

import android.accounts.Account;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */
public final class PasswordManager {
    static final String KEY = "PASSWORD_KEY";

    private final String mStoragePassword;
    private final Context mContext;

    private PasswordManager(Context context, String storagePassword) {
        mContext = context;
        mStoragePassword = storagePassword;
    }

    public static PasswordManager create(Context context) {
        String storagePassword = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return new PasswordManager(context, storagePassword);
    }

    public void put(Account account, String plainPassword) {
        initHawk(account);
        Hawk.put(KEY, plainPassword);
    }

    @Nullable
    public String get(Account account) {
        initHawk(account);
        try {
            return Hawk.get(KEY);
        } catch (Exception ex) {
            // Swallow any exception that pop ups
            return null;
        }
    }

    private void initHawk(Account account) {
        Hawk.init(mContext)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                .setStorage(AccountStorage.create(mContext, account))
                .setPassword(mStoragePassword)
                .build();
    }
}
