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
 * <http://www.gnu.org/licenses/lgpl>./
 */
package com.jaspersoft.android.jaspermobile.db.migrate;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.sdk.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import timber.log.Timber;

/**
 * Populating 'account_name' field with data from server profiles.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class SavedItemsMigration implements Migration {
    public static final String SHARED_DIR = "com.jaspersoft.account.none";

    private static final String TAG = JasperAccountManager.class.getSimpleName();
    private final Context mContext;

    public SavedItemsMigration(Context context) {
        mContext = context;
        Timber.tag(TAG);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        database.execSQL(
                "CREATE TABLE saved_items ( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " file_path TEXT NOT NULL, name TEXT NOT NULL," +
                        " file_format TEXT NOT NULL, description TEXT, " +
                        "wstype TEXT NOT NULL DEFAULT 'unknown'," +
                        " account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none'," +
                        " creation_time NUMERIC NOT NULL )"
        );
        migrateSavedItems(database);
    }

    private void migrateSavedItems(SQLiteDatabase db) {
        File savedItemsDir = getSavedItemsDir();
        if (savedItemsDir != null) {
            File[] savedItems = savedItemsDir.listFiles();

            File sharedDir = createShareDirectory(savedItemsDir);
            if (sharedDir != null) {
                if (savedItems.length > 0) {
                    moveToSharedDir(savedItems, sharedDir);
                    saveSharedFilesInDb(db, sharedDir);
                }
            }
        }
    }

    @Nullable
    private File createShareDirectory(File savedItemsDir) {
        File sharedDir = new File(savedItemsDir, SHARED_DIR);
        if (!sharedDir.exists() && !sharedDir.mkdir()) return null;
        return sharedDir;
    }

    @Nullable
    private File getSavedItemsDir() {
        File appFilesDir = mContext.getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, "saved.reports");

        if (!savedReportsDir.exists()) {
            boolean created = savedReportsDir.mkdir();
            if (!created) {
                Timber.w("Unable to create %s", savedReportsDir);
                return null;
            }
        }
        return savedReportsDir;
    }

    private void moveToSharedDir(File[] savedItems, File sharedDir) {
        for (File savedItem : savedItems) {
            try {
                org.apache.commons.io.FileUtils
                        .moveFileToDirectory(savedItem, sharedDir, false);
            } catch (IOException e) {
                Timber.w(e, "Failed to move file to shared destination");
            }
        }
    }

    private void saveSharedFilesInDb(SQLiteDatabase db, File sharedDir) {
        ContentValues contentValues = new ContentValues();
        File[] savedItems = sharedDir.listFiles();

        String fileName, fileFormat;
        long creationTime;
        for (File savedItem : savedItems) {
            fileName = FileUtils.getBaseName(savedItem.getName());
            fileFormat = FileUtils.getExtension(savedItem.getName()).toUpperCase(Locale.getDefault());
            creationTime = savedItem.lastModified();

            contentValues.clear();
            contentValues.put("file_path", savedItem.getPath());
            contentValues.put("name", fileName);
            contentValues.put("file_format", fileFormat);
            contentValues.put("creation_time", creationTime);

            db.insert("saved_items", null, contentValues);
        }
    }
}
