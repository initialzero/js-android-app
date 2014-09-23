/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.4
 */
@Deprecated
public final class ReportOptions implements BaseColumns {
    // This class cannot be instantiated
    private ReportOptions() {}

    public static final String TABLE_NAME = "old_report_options";

    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE = "value";
    public static final String KEY_IS_LIST_ITEM = "is_list_item";
    public static final String KEY_SERVER_PROFILE_ID = "server_profile_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_ORGANIZATION = "organization";
    public static final String KEY_REPORT_URI = "report_uri";

    public static final String TABLE_CREATE_SQL =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NAME              + " TEXT NOT NULL, "
                    + KEY_VALUE             + " TEXT NOT NULL, "
                    + KEY_IS_LIST_ITEM      + " INTEGER NOT NULL DEFAULT 0, "
                    + KEY_SERVER_PROFILE_ID + " INTEGER NOT NULL, "
                    + KEY_USERNAME          + " TEXT NOT NULL, "
                    + KEY_ORGANIZATION      + " TEXT, "
                    + KEY_REPORT_URI        + " TEXT NOT NULL "
                    + ");";

}
