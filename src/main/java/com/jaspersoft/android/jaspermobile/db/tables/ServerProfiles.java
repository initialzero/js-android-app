/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile.db.tables;

import android.provider.BaseColumns;

/**
 * Convenience definitions for the Server Profiles table
 *
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public final class ServerProfiles implements BaseColumns {
    // This class cannot be instantiated
    private ServerProfiles() {}

    public static final String TABLE_NAME = "server_profiles";

    public static final String KEY_ALIAS = "alias";
    public static final String KEY_SERVER_URL = "server_url";
    public static final String KEY_ORGANIZATION = "organization";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    public static final String TABLE_CREATE_SQL =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_ALIAS         + " TEXT NOT NULL, "
                    + KEY_SERVER_URL    + " TEXT NOT NULL, "
                    + KEY_ORGANIZATION  + " TEXT NOT NULL, "
                    + KEY_USERNAME      + " TEXT NOT NULL, "
                    + KEY_PASSWORD      + " TEXT NOT NULL "
                    + ");";

}
