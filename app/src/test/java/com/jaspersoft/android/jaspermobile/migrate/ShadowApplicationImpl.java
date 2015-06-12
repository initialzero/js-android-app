/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.migrate;

import android.app.Application;

import org.apache.commons.io.FileUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Just relocate the database file to a hard defined position.
 */
@Implements(Application.class)
public class ShadowApplicationImpl extends ShadowApplication {

    private final static URL DATABASE_1_9_URL = ShadowApplicationImpl.class.getClassLoader().getResource("jasper_mobile_db_1.9");
    private static File DATABASE_1_9;

    static {
        try {
            DATABASE_1_9 = new File(DATABASE_1_9_URL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Implementation
    public File getDatabasePath(String name) {
        File parent = DATABASE_1_9.getParentFile();
        File database = new File(parent, "jasper_mobile_db");

        if (!database.exists()) {
            try {
                assertThat(database.createNewFile(), is(true));
                FileUtils.copyFile(DATABASE_1_9, database);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return database;
    }

    @Override
    @Implementation
    public File getExternalFilesDir(String name) {
        File parent = DATABASE_1_9.getParentFile();
        File external = new File(parent, "external_dir");
        if (!external.exists()) {
            assertThat(external.mkdir(), is(true));
        }
        return external;
    }
}