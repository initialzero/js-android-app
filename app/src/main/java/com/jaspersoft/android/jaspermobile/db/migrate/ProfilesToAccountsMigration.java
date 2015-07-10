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

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.util.GeneralPref_;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import java.util.List;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class ProfilesToAccountsMigration implements Migration {
    private final Context mContext;

    ProfilesToAccountsMigration(Context context) {
        mContext = context;
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        adaptProfilesToAccounts(database);
        activateAccount(database);
    }

    private void adaptProfilesToAccounts(SQLiteDatabase db) {
        AccountServerData data;
        JasperAccountManager util = JasperAccountManager.get(mContext);
        Cursor cursor = db.query(ServerProfilesTable.TABLE_NAME,
                ServerProfilesTable.ALL_COLUMNS, null, null, null, null, null);
        try {
            List<ServerProfiles> profiles = ServerProfiles.listFromCursor(cursor);
            Timber.d("The number of previously saved accounts are: " + profiles.size());
            for (ServerProfiles profile : profiles) {
                try {
                    data = new AccountServerData()
                            .setAlias(profile.getAlias())
                            .setServerUrl(profile.getServerUrl())
                            .setOrganization(profile.getOrganization())
                            .setUsername(profile.getUsername())
                            .setPassword(profile.getPassword())
                            .setEdition(TextUtils.isEmpty(profile.getEdition()) ? "?" : profile.getEdition())
                            .setVersionName(String.valueOf(profile.getVersionCode()));
                    util.addAccountExplicitly(data).subscribe();
                } catch (IllegalArgumentException ex) {
                    Timber.w(ex, "Mis-configured profile '" + profile.getAlias() + "' skipping it");
                }
            }
        } finally {
            cursor.close();
        }
    }

    private void activateAccount(SQLiteDatabase db) {
        GeneralPref_ pref = new GeneralPref_(mContext);
        long profileId = pref.currentProfileId().get();
        Cursor cursor = db.query(ServerProfilesTable.TABLE_NAME,
                ServerProfilesTable.ALL_COLUMNS,
                ServerProfilesTable._ID + "=" + profileId,
                null, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ServerProfiles profile = new ServerProfiles(cursor);
                Account account = new Account(profile.getAlias(), JasperSettings.JASPER_ACCOUNT_TYPE);
                JasperAccountManager.get(mContext).activateAccount(account);
                Timber.d("Account[" + account + "] was activated");
            }
        } finally {
            cursor.close();
        }
    }
}
