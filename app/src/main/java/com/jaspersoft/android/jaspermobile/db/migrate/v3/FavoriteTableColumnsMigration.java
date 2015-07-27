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
package com.jaspersoft.android.jaspermobile.db.migrate.v3;

import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.migrate.Migration;

/**
 * Removing column 'name'
 *
 * @author Tom Koptel
 * @since 2.0
 */
final class FavoriteTableColumnsMigration implements Migration {
    @Override
    public void migrate(SQLiteDatabase database) {
        addAccountNameColumn(database);
        addCreationTimeColumn(database);
    }

    private void addAccountNameColumn(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE favorites ADD COLUMN account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none';");
    }

    private void addCreationTimeColumn(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE favorites ADD COLUMN creation_time TEXT DEFAULT '';");
    }
}
