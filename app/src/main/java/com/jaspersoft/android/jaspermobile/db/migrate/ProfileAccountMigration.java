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
 * <http://www.gnu.org/licenses/lgpl>./
 */

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.util.GeneralPref_;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ProfileAccountMigration implements Migration {
    private final Context mContext;

    public ProfileAccountMigration(Context context) {
        this.mContext = context;
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        migrateOldProfiles(database);
        activateProfile(database);
    }

    private void migrateOldProfiles(SQLiteDatabase db) {
        AccountServerData data;
        AccountManagerUtil util = AccountManagerUtil.get(mContext);
        Cursor cursor = db.query(ServerProfilesTable.TABLE_NAME,
                ServerProfilesTable.ALL_COLUMNS, null, null, null, null, null);
        try {
            List<ServerProfiles> profiles = ServerProfiles.listFromCursor(cursor);
            for (ServerProfiles profile : profiles) {
                data = new AccountServerData()
                        .setAlias(profile.getAlias())
                        .setServerUrl(profile.getServerUrl())
                        .setOrganization(profile.getOrganization())
                        .setUsername(profile.getUsername())
                        .setPassword(profile.getPassword())
                        .setEdition(profile.getEdition())
                        .setVersionName(profile.getVersionCode() + "");
                util.addAccountExplicitly(data).subscribe();
            }
        } finally {
            cursor.close();
        }
    }

    private void activateProfile(SQLiteDatabase db) {
        GeneralPref_ pref = new GeneralPref_(mContext);
        long profileId = pref.currentProfileId().get();
        Cursor cursor = db.query(ServerProfilesTable.TABLE_NAME,
                ServerProfilesTable.ALL_COLUMNS,
                ServerProfilesTable._ID + "=" + profileId,
                null, null, null, null);
        try {
            Account account;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ServerProfiles profile = new ServerProfiles(cursor);
                account = new Account(profile.getAlias(), JasperSettings.JASPER_ACCOUNT_TYPE);
            } else {
                account = new Account(AccountServerData.Demo.ALIAS,
                        JasperSettings.JASPER_ACCOUNT_TYPE);
            }
            BasicAccountProvider.get(mContext).putAccount(account);
        } finally {
            cursor.close();
        }
    }
}
