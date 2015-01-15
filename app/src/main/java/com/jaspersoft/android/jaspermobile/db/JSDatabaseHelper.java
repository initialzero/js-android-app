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

package com.jaspersoft.android.jaspermobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.JasperMobileDbDatabase;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class JSDatabaseHelper extends JasperMobileDbDatabase {
    private static final String TAG = JSDatabaseHelper.class.getSimpleName();
    private final Context mContext;

    public JSDatabaseHelper(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        seedData(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        executePragmas(db);
        switch (oldVersion) {
            case 1:
            case 2:
                db.execSQL("DROP TABLE IF EXISTS report_options;");

                db.execSQL("ALTER TABLE server_profiles RENAME TO tmp_server_profiles;");
                db.execSQL(
                        "CREATE TABLE server_profiles ( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                " alias TEXT, server_url TEXT, organization TEXT, username TEXT," +
                                " password TEXT, edition TEXT, version_code NUMERIC );"
                );
                db.execSQL("INSERT INTO server_profiles(alias, server_url, organization, username, password)" +
                        " select alias, server_url, organization, username, password from tmp_server_profiles;");
                db.execSQL("DROP TABLE IF EXISTS tmp_server_profiles;");

                db.execSQL("ALTER TABLE favorites RENAME TO tmp_favorites;");
                db.execSQL(
                        "CREATE TABLE favorites ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT," +
                                " title TEXT, uri TEXT, description TEXT, wstype TEXT, username TEXT, " +
                                "organization TEXT, server_profile_id INTEGER REFERENCES server_profiles(_id)" +
                                " ON DELETE CASCADE )"
                );
                db.execSQL("INSERT INTO favorites(name, title, uri, description, wstype, username, organization, server_profile_id)" +
                        " select name, title, uri, description, wstype, username, organization, server_profile_id from tmp_favorites;");
                db.execSQL("DROP TABLE IF EXISTS tmp_favorites;");
            case 3:
                db.execSQL("ALTER TABLE server_profiles RENAME TO tmp_server_profiles;");
                db.execSQL(
                        "CREATE TABLE server_profiles ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "alias TEXT NOT NULL UNIQUE, server_url TEXT NOT NULL, " +
                                "organization TEXT, edition TEXT, created_at REAL DEFAULT CURRENT_TIMESTAMP," +
                                " version_code REAL )"
                );
                db.execSQL("INSERT INTO server_profiles(alias, server_url, organization, edition, version_code)" +
                        " select alias, server_url, organization, edition, version_code from tmp_server_profiles;");
                db.execSQL("DROP TABLE IF EXISTS tmp_server_profiles;");
                db.execSQL("ALTER TABLE favorites ADD COLUMN creation_time TEXT DEFAULT '';");
                break;
        }
    }

    private void seedData(SQLiteDatabase db) {
        populateDefaultServer(db);
        populateTestServers(db);
    }

    private void populateDefaultServer(SQLiteDatabase db) {
        ServerProfiles defaultProfile = new ServerProfiles()
                .withAlias(AccountServerData.Demo.ALIAS)
                .withServerUrl(AccountServerData.Demo.SERVER_URL)
                .withOrganization(AccountServerData.Demo.ORGANIZATION);
        db.insert(ServerProfilesTable.TABLE_NAME, null, defaultProfile.getContentValues());
    }

    private void populateTestServers(SQLiteDatabase db) {
        InputStream is = mContext.getResources().openRawResource(R.raw.profiles);

        // This is possible during unit testing
        // As soon as we don`t care about test data at that stage
        // we are simply ignoring step
        if (is == null) return;

        try {
            String json = IOUtils.toString(is);
            Gson gson = new Gson();
            Profiles profiles = gson.fromJson(json, Profiles.class);
            for (ServerProfiles profile : profiles.getData()) {
                // We need populate content values manually ;(
                profile
                       .withAlias(profile.getAlias())
                       .withServerUrl(profile.getServerUrl())
                       .withOrganization(profile.getOrganization());

                ContentValues contentValues = profile.getContentValues();
                db.insert(ServerProfilesTable.TABLE_NAME, null, contentValues);
            }
        } catch (IOException e) {
            Log.w(TAG, "Ignoring population of data");
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static class Profiles {
        private List<ServerProfiles> profiles;

        public List<ServerProfiles> getData() {
            return profiles;
        }
    }

}
