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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.sdk.util.FileUtils;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

/**
 * Populating 'account_name' field with data from server profiles.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class SavedItemsMigration implements Migration {
    private static final String TAG = JasperAccountManager.class.getSimpleName();
    private final Context mContext;

    public SavedItemsMigration(Context context) {
        mContext = context;
        Timber.tag(TAG);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE saved_items ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "file_path TEXT, name TEXT, file_format TEXT, description TEXT, wstype TEXT, " +
                "username TEXT, organization TEXT, account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none', creation_time NUMERIC );");
        migrateSavedItems(database);
    }

    private void migrateSavedItems(SQLiteDatabase db){
        File savedItemsDir = getSavedItemsDir();
        File sharedDir = new File(savedItemsDir, "com.jaspersoft.account.none");
        if(!sharedDir.exists() && !sharedDir.mkdir()) return;
        for (File savedItemDir : savedItemsDir.listFiles()) {

            String fileName = FileUtils.getBaseName(savedItemDir.getName());
            String fileFormat = FileUtils.getExtension(savedItemDir.getName()).toUpperCase(Locale.getDefault());
            long creationTime = savedItemDir.lastModified();
            File newFilePath = new File(sharedDir, fileName);

            boolean movedSuccess = savedItemDir.renameTo(newFilePath);
            File saveditemFile = new File(newFilePath, fileName + "." + fileFormat);
            if(movedSuccess && saveditemFile.exists()) {
                db.execSQL("INSERT INTO saved_items ( file_path, name, file_format, creation_time, account_name ) VALUES ( "
                        + "'" + saveditemFile.getPath() + "', "
                        + "'" + fileName + "', "
                        + "'" + fileFormat + "', "
                        + creationTime + ", "
                        + "com.jaspersoft.account.none");
            }
        }
    }

    private File getSavedItemsDir(){
        File appFilesDir = mContext.getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, "saved.reports");

        if (!savedReportsDir.exists()) {
            Timber.w("Unable to create %s", savedReportsDir);
            return null;
        }

        return savedReportsDir;
    }
}
