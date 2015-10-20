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

package com.jaspersoft.android.jaspermobile.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.database.JasperMobileDbDatabase;
import com.jaspersoft.android.jaspermobile.db.migrate.v2.MigrationV2;
import com.jaspersoft.android.jaspermobile.db.migrate.v3.MigrationV3;
import com.jaspersoft.android.jaspermobile.db.migrate.v4.MigrationV4;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class JSDatabaseHelper extends JasperMobileDbDatabase {
    private final Context mContext;

    public JSDatabaseHelper(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        executePragmas(db);
        switch (oldVersion) {
            case 1:
            case 2:
                new MigrationV2().migrate(db);
            case 3:
                new MigrationV3(mContext).migrate(db);
            case 4:
                new MigrationV4(mContext).migrate(db);
                break;
        }
    }
}
