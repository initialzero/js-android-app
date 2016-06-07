/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.app.Application;

import com.jaspersoft.android.jaspermobile.test.support.TestResource;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;

/**
 * Just relocate the database file to a hard defined position.
 */
@Implements(Application.class)
public class ShadowApplicationImpl extends ShadowApplication {

    public ShadowApplicationImpl() {}

    @Override
    @Implementation
    public File getExternalFilesDir(String name) {
        File dbFile = TestResource.get("jasper_mobile_db_1.9").asFile();
        File parentDir = dbFile.getParentFile();
        File externalDir = new File(parentDir, "external_dir");
        if (!externalDir.exists()) {
            boolean result = externalDir.mkdir();
            if (!result) {
                throw new RuntimeException("Failed to create external dir");
            }
        }
        return externalDir;
    }
}