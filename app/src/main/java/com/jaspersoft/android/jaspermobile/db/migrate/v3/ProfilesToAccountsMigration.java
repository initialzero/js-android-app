/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.db.migrate.v3;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.db.migrate.Migration;
import com.jaspersoft.android.jaspermobile.util.GeneralPref_;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class ProfilesToAccountsMigration implements Migration {
    private final static String TABLE_NAME = "server_profiles";

    private final static String _ID = "_id";
    private final static String ALIAS = "alias";
    private final static String SERVER_URL = "server_url";
    private final static String ORGANIZATION = "organization";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String EDITION = "edition";
    private final static String VERSION_CODE = "version_code";
    private final static String[] ALL_COLUMNS = new String[]{_ID, ALIAS, SERVER_URL, ORGANIZATION, USERNAME, PASSWORD, EDITION, VERSION_CODE};

    private final Context mContext;
    private final AccountManager mAccountManager;

    ProfilesToAccountsMigration(Context context) {
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        adaptProfilesToAccounts(database);
        activateAccount(database);
    }

    private void adaptProfilesToAccounts(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    adaptProfile(cursor);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
    }

    private void adaptProfile(Cursor cursor) {
        String alias = cursor.getString(cursor.getColumnIndex(ALIAS));
        String serverUrl = cursor.getString(cursor.getColumnIndex(SERVER_URL));
        String organization = cursor.getString(cursor.getColumnIndex(ORGANIZATION));
        String username = cursor.getString(cursor.getColumnIndex(USERNAME));
        String password = cursor.getString(cursor.getColumnIndex(PASSWORD));
        String versionName = cursor.getString(cursor.getColumnIndex(VERSION_CODE));

        String edition = cursor.getString(cursor.getColumnIndex(EDITION));
        edition = TextUtils.isEmpty(edition) ? "?" : edition;

        Account account = new Account(alias, "com.jaspersoft");
        mAccountManager.addAccountExplicitly(account, password, null);

        mAccountManager.setUserData(account, "ALIAS_KEY", alias);
        mAccountManager.setUserData(account, "SERVER_URL_KEY", serverUrl);
        mAccountManager.setUserData(account, "ORGANIZATION_KEY", organization);
        mAccountManager.setUserData(account, "USERNAME_KEY", username);
        mAccountManager.setUserData(account, "EDITION_KEY", edition);
        mAccountManager.setUserData(account, "VERSION_NAME_KEY", versionName);
    }

    private void activateAccount(SQLiteDatabase db) {
        GeneralPref_ pref = new GeneralPref_(mContext);
        long profileId = pref.currentProfileId().get();
        Cursor cursor = db.query(TABLE_NAME,
                ALL_COLUMNS, _ID + "=" + profileId,
                null, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String alias = cursor.getString(cursor.getColumnIndex(ALIAS));
                SharedPreferences preference = mContext.getSharedPreferences("JasperAccountManager", Activity.MODE_PRIVATE);
                preference.edit().putString("ACCOUNT_NAME_KEY", alias).apply();
            }
        } finally {
            cursor.close();
        }
    }
}
