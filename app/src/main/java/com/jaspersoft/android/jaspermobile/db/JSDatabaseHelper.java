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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.database.JasperMobileDbDatabase;


public class JSDatabaseHelper extends JasperMobileDbDatabase {

    public JSDatabaseHelper(Context context) {
        super(context);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        executePragmas(db);
        if (oldVersion == 2 && newVersion == 3) {
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
        }
    }

}
