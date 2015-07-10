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
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.support.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class ResourceDatabase {

    private final URL resourceUrl;
    private final String resourceName;
    private final ClassLoader classLoader;

    public static ResourceDatabase get(String relativePath) {
        return new ResourceDatabase(relativePath);
    }

    private ResourceDatabase(String name) {
        resourceName = name;
        classLoader = ResourceDatabase.class.getClassLoader();
        resourceUrl = classLoader.getResource(resourceName);
    }

    //---------------------------------------------------------------------
    // Getters
    //---------------------------------------------------------------------

    public InputStream getInputStream() {
        return classLoader.getResourceAsStream(resourceName);
    }

    public File getFile() {
        return new File(getFilePath());
    }

    //---------------------------------------------------------------------
    // DB file manipulations
    //---------------------------------------------------------------------

    public void delete() {
        File databaseFile = getFile();
        File journalFile = new File(databaseFile.getParent(), databaseFile.getName() + "-journal");
        removeFileOrThrow(databaseFile);
        removeFileOrThrow(journalFile);
    }

    //---------------------------------------------------------------------
    // DB actions
    //---------------------------------------------------------------------

    public void performAction(DbAction action) {
        SQLiteDatabase db = getDatabase();
        try {
            action.performAction(db);
        } finally {
            db.close();
        }
    }

    public void performMigration(final SQLiteOpenHelper sqLiteOpenHelper, final MigrationCondition migrationCondition) {
        performAction(new DbAction() {
            @Override
            public void performAction(SQLiteDatabase database) {
                sqLiteOpenHelper.onUpgrade(database, migrationCondition.oldVersion(), migrationCondition.newVersion());
            }
        });
    }

    public void performSql(final String sql) {
        performAction(new DbAction() {
            @Override
            public void performAction(SQLiteDatabase database) {
                database.execSQL(sql);
            }
        });
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private SQLiteDatabase getDatabase() {
        return SQLiteDatabase.openDatabase(getFile().getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
    }

    private void removeFileOrThrow(File file) {
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("File to remove file: " + file.getAbsolutePath());
            }
        }
    }

    private String getFilePath() {
        try {
            return resourceUrl.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public interface DbAction {
        void performAction(SQLiteDatabase database);
    }
}
