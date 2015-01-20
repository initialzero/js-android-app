/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class DatabaseUtils {
    public static final String TEST_ALIAS = "Test Demo";
    public static final String TEST_ORGANIZATION = "test_organization";
    public static final String TEST_SERVER_URL = "http://mobiledemo.jaspersoft.com/jasperserver-pro";
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASS = "testuser";

    private DatabaseUtils() {
        throw new AssertionError();
    }

    public static long createTestProfile(ContentResolver contentResolver) {
        ServerProfiles serverProfile = new ServerProfiles();
        serverProfile.setAlias(TEST_ALIAS);
        serverProfile.setServerUrl(TEST_SERVER_URL);
        serverProfile.setOrganization(TEST_ORGANIZATION);

        Uri uri = contentResolver.insert(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, serverProfile.getContentValues());
        return Long.valueOf(uri.getLastPathSegment());
    }

    public static void updateProfile(ContentResolver contentResolver, long id, String column, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(column, value);
        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, String.valueOf(id));
        contentResolver.update(uri, contentValues, null, null);
    }

    public static long createDefaultProfile(ContentResolver contentResolver) {
        ServerProfiles serverProfile = new ServerProfiles();
        serverProfile.setAlias(AccountServerData.Demo.ALIAS);
        serverProfile.setServerUrl(AccountServerData.Demo.SERVER_URL);
        serverProfile.setOrganization(AccountServerData.Demo.ORGANIZATION);
        serverProfile.setEdition("PRO");
        serverProfile.setVersionCode(5.5d);

        Uri uri = contentResolver.insert(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, serverProfile.getContentValues());
        return Long.valueOf(uri.getLastPathSegment());
    }

    public static void deleteAllProfiles(ContentResolver contentResolver) {
        contentResolver.delete(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, null, null);
    }

    public static void deleteAllFavorites(ContentResolver contentResolver) {
        contentResolver.delete(JasperMobileDbProvider.FAVORITES_CONTENT_URI, null, null);
    }

    public static Cursor getAllFavorites(ContentResolver contentResolver) {
        return contentResolver.query(JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                FavoritesTable.ALL_COLUMNS, null, null, null);
    }

    public static void createOnlyDefaultProfile(ContentResolver contentResolver) {
        deleteAllProfiles(contentResolver);
        createDefaultProfile(contentResolver);
    }
        public static void deleteTestProfiles(ContentResolver contentResolver) {
        contentResolver.delete(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI, null, null);
        createDefaultProfile(contentResolver);
    }

    public static Cursor queryTestProfile(ContentResolver contentResolver) {
        String selection = ServerProfilesTable.ALIAS + "= ?";
        String[] selectionArgs = {DatabaseUtils.TEST_ALIAS};
        return contentResolver.query(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                ServerProfilesTable.ALL_COLUMNS, selection, selectionArgs, null);
    }

    public static void deleteAllSavedItems(ContentResolver contentResolver) {
        contentResolver.delete(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI, null, null);
    }

    @Nullable
    public static ServerProfiles queryProfileByAlias(ContentResolver contentResolver, String alias) {
        String selection = ServerProfilesTable.ALIAS + "= ?";
        String[] selectionArgs = {alias};
        Cursor cursor = contentResolver.query(JasperMobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                ServerProfilesTable.ALL_COLUMNS, selection, selectionArgs, null);
        if (cursor == null) return null;
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToPosition(0);
        return new ServerProfiles(cursor);
    }
}
