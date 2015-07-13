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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.migrate.Migration;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class LegacyProfileMigration implements Migration {
    private static final String LEGACY_MOBILE_DEMO = "http://mobiledemo.jaspersoft.com/jasperserver-pro";
    private static final String NEW_MOBILE_DEMO = "http://mobiledemo2.jaspersoft.com/jasperserver-pro";
    private static final String LEGACY_NAME = "Legacy Mobile Demo";

    @Override
    public void migrate(SQLiteDatabase database) {
        updateLegacyProfile(database);
    }

    private void updateLegacyProfile(SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("alias", LEGACY_NAME);
        contentValues.put("server_url", NEW_MOBILE_DEMO);
        database.update("server_profiles", contentValues, "server_url=?", new String[] {LEGACY_MOBILE_DEMO});
    }
}
