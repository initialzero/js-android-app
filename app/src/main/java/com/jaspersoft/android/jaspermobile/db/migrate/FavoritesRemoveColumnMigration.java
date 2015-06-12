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
 * <http://www.gnu.org/licenses/lgpl>./
 */
package com.jaspersoft.android.jaspermobile.db.migrate;

import android.database.sqlite.SQLiteDatabase;

/**
 * Removing column 'name'
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class FavoritesRemoveColumnMigration implements Migration {
    @Override
    public void migrate(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE favorites RENAME TO tmp_favorites;");

        database.execSQL(
                "CREATE TABLE favorites ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, uri TEXT, " +
                        "description TEXT, wstype TEXT, username TEXT, organization TEXT, account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none', creation_time TEXT )"
        );
        database.execSQL("INSERT INTO favorites(title, uri, description, wstype, username, organization, account_name, creation_time)" +
                " select title, uri, description, wstype, username, organization, account_name, creation_time from tmp_favorites;");

        database.execSQL("DROP TABLE IF EXISTS tmp_favorites;");
    }
}
