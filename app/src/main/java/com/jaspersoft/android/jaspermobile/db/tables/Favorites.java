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
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.db.tables;

import android.provider.BaseColumns;

/**
 * Convenience definitions for the Favorites table
 *
 * @author Oleg Gavavka
 * @version $Id$
 * @since 1.0
 */
@Deprecated
public final class Favorites implements BaseColumns {
    // This class cannot be instantiated
    private Favorites() {}

    public static final String TABLE_NAME = "old_favorites";

    public static final String KEY_NAME = "name";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_URI = "uri";
    public static final String KEY_WSTYPE = "wstype";
    public static final String KEY_SERVER_PROFILE_ID = "server_profile_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_ORGANIZATION = "organization";

    public static final String TABLE_CREATE_SQL =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NAME              + " TEXT NOT NULL, "
                    + KEY_TITLE             + " TEXT NOT NULL, "
                    + KEY_DESCRIPTION       + " TEXT, "
                    + KEY_URI               + " TEXT NOT NULL, "
                    + KEY_WSTYPE            + " TEXT NOT NULL, "
                    + KEY_SERVER_PROFILE_ID + " INTEGER NOT NULL, "
                    + KEY_USERNAME          + " TEXT NOT NULL, "
                    + KEY_ORGANIZATION      + " TEXT "
                    + ");";

}
