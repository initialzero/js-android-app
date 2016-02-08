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

package com.jaspersoft.android.jaspermobile.db.migrate.v5;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.migrate.Migration;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public final class MigrationV5 implements Migration {

    private static final String EDITION_KEY = "EDITION_KEY";

    private final AccountManager mAccountManager;

    public MigrationV5(Context context) {
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE saved_items ADD COLUMN downloaded NUMERIC DEFAULT 1");
        migrateEditionInAccounts();
    }

    private void migrateEditionInAccounts() {
        Account[] accounts = mAccountManager.getAccountsByType("com.jaspersoft");
        for (Account account : accounts) {
            adaptEdition(account);
        }
    }

    private void adaptEdition(Account account) {
        String edition = mAccountManager.getUserData(account, EDITION_KEY);
        boolean isPro = "PRO".equals(edition);
        mAccountManager.setUserData(account, EDITION_KEY, String.valueOf(isPro));
    }
}
