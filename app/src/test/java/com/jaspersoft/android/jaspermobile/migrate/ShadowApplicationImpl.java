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

package com.jaspersoft.android.jaspermobile.migrate;

import android.app.Application;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    @Override
    @Implementation
    public File getDatabasePath(String name) {
        File parent = DATABASE_1_9.getParentFile();
        File database = new File(parent, "jasper_mobile_db");

        if (!database.exists()) {
            try {
                assertThat(database.createNewFile(), is(true));
                copy(DATABASE_1_9, database);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return database;
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}