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

package com.jaspersoft.android.jaspermobile.test.support;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.robolectric.Robolectric;

import java.io.File;

public class DatabaseSpecification extends UnitTestSpecification {
    private Context mContext;

    @Before
    public void initContext() {
        mContext = Robolectric.application;
        resetDatabase();
    }

    @After
    public void clearDatabase() {
        resetDatabase();
    }

    public void resetDatabase() {
        File dbFile = mContext.getDatabasePath(null);
        SQLiteDatabase.deleteDatabase(dbFile);
    }

    public Context getContext() {
        return mContext;
    }

    public ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }
}