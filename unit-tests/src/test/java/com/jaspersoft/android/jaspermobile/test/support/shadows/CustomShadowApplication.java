/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.support.shadows;

import android.app.Application;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;
import java.io.IOException;

/**
 * Just relocate the database file to a hard defined position.
 */
@Implements(Application.class)
public class CustomShadowApplication extends ShadowApplication {

    public static final String ALTERNATIVE_DB_PATH = "build/resources/unit-test.db";
    private File database = new File(ALTERNATIVE_DB_PATH);
    private static boolean mPragmaSet;

    @Override
    @Implementation
    public File getDatabasePath(String name) {
        if (!database.exists()) {
            try {
                database.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return database;
    }

    @Implementation
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler databaseErrorHandler) {
        SQLiteDatabase database = super.openOrCreateDatabase(name, mode, factory, databaseErrorHandler);
        if (!mPragmaSet) {
            database.execSQL("PRAGMA foreign_keys=ON;");
            mPragmaSet = true;
        }
        return database;
    }

}