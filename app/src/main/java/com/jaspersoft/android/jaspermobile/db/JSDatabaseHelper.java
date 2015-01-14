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

package com.jaspersoft.android.jaspermobile.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.db.database.JasperMobileDbDatabase;
import com.jaspersoft.android.sdk.util.FileUtils;

import java.io.File;

import roboguice.util.Ln;


public class JSDatabaseHelper extends JasperMobileDbDatabase {

    private final Context mContext;

    public JSDatabaseHelper(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        executePragmas(db);
        switch(oldVersion) {
            case 1:
            case 2:
                db.execSQL("DROP TABLE IF EXISTS report_options;");

                db.execSQL("ALTER TABLE server_profiles RENAME TO tmp_server_profiles;");
                db.execSQL(
                        "CREATE TABLE server_profiles ( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                " alias TEXT, server_url TEXT, organization TEXT, username TEXT," +
                                " password TEXT, edition TEXT, version_code NUMERIC );"
                );
                db.execSQL("INSERT INTO server_profiles(alias, server_url, organization, username, password)" +
                        " select alias, server_url, organization, username, password from tmp_server_profiles;");
                db.execSQL("DROP TABLE IF EXISTS tmp_server_profiles;");

                db.execSQL("ALTER TABLE favorites RENAME TO tmp_favorites;");
                db.execSQL(
                        "CREATE TABLE favorites ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT," +
                                " title TEXT, uri TEXT, description TEXT, wstype TEXT, username TEXT, " +
                                "organization TEXT, server_profile_id INTEGER REFERENCES server_profiles(_id)" +
                                " ON DELETE CASCADE )"
                );
                db.execSQL("INSERT INTO favorites(name, title, uri, description, wstype, username, organization, server_profile_id)" +
                        " select name, title, uri, description, wstype, username, organization, server_profile_id from tmp_favorites;");
                db.execSQL("DROP TABLE IF EXISTS tmp_favorites;");
            case 3:
                db.execSQL("ALTER TABLE favorites ADD COLUMN creation_time TEXT DEFAULT '';");
                db.execSQL(
                        "CREATE TABLE saved_items ( _id INTEGER PRIMARY KEY AUTOINCREMENT, file_path TEXT, name TEXT, file_format TEXT, " +
                                "description TEXT, wstype TEXT, username TEXT, organization TEXT, creation_time NUMERIC, server_profile_id INTEGER REFERENCES server_profiles(_id) )"
                );
                migrateSavedItems(db);
                break;
        }
    }

    private File getSavedItemsDir(){
        File appFilesDir = mContext.getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, "saved.reports");

        if (!savedReportsDir.exists()) {
            Ln.e("Unable to create %s", savedReportsDir);
            return null;
        }

        return savedReportsDir;
    }

    private void migrateSavedItems(SQLiteDatabase db){
        File savedItemsDir = getSavedItemsDir();
        File sharedDir = new File(savedItemsDir, "-1");
        if(!sharedDir.exists() && !sharedDir.mkdir()) return;
        for (File savedItemDir : savedItemsDir.listFiles()) {

            String fileName = FileUtils.getBaseName(savedItemDir.getName());
            String fileFormat = FileUtils.getExtension(savedItemDir.getName()).toUpperCase();
            long creationTime = savedItemDir.lastModified();
            File newFilePath = new File(sharedDir, fileName);

            boolean movedSuccess = savedItemDir.renameTo(newFilePath);
            File saveditemFile = new File(newFilePath, fileName + "." + fileFormat);
            if(movedSuccess && saveditemFile.exists()) {
                db.execSQL("INSERT INTO saved_items ( file_path, name, file_format, creation_time, server_profile_id ) VALUES ( "
                        + "'" + saveditemFile.getPath() + "', "
                        + "'" + fileName + "', "
                        + "'" + fileFormat + "', "
                        + creationTime + ", "
                        + "-1)");
            }
        }
    }

}
