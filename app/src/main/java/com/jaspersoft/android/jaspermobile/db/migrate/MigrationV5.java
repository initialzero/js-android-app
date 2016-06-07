/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class MigrationV5 implements Migration {
    private static final String JASPER_ACCOUNT_TYPE = "com.jaspersoft";
    private static final String JASPER_AUTH_TOKEN_TYPE = "FULL ACCESS";

    private final Context mContext;

    public MigrationV5(Context context) {
        mContext = context;
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(JASPER_ACCOUNT_TYPE);
        for (Account account : accounts) {
            String token = accountManager.peekAuthToken(account, JASPER_AUTH_TOKEN_TYPE);
            if (!TextUtils.isEmpty(token)){
                accountManager.invalidateAuthToken(JASPER_ACCOUNT_TYPE, token);
            }
        }
    }
}
