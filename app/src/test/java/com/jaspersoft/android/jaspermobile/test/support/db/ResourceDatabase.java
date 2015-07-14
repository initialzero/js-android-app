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

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class ResourceDatabase {

    private final URL resourceUrl;
    private final String resourceName;
    private final ClassLoader classLoader;
    private SQLiteDatabase mDatabase;

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

    public SQLiteDatabase open() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = SQLiteDatabase.openDatabase(getFile().getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        }
        return mDatabase;
    }

    public void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public SQLiteDatabase getDb() {
        if (mDatabase == null) {
            throw new IllegalStateException("Database is not opened!");
        }
        return mDatabase;
    }

    public void performSql(final String sql) {
        if (mDatabase == null) {
            throw new IllegalStateException("Database is not opened!");
        }
        mDatabase.execSQL(sql);
    }

    public void execSQLResource(RawSqlStatements rawSql) {
        for (String sql : rawSql.getStatements()) {
            performSql(sql);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

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

    public interface RawSqlStatements {
        Collection<String> getStatements();
    }
}
