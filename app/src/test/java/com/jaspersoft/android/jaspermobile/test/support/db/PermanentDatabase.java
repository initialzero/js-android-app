/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.support.db;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class PermanentDatabase {

    private final ResourceDatabase resourceDatabase;

    private PermanentDatabase(ResourceDatabase resourceDatabase) {
        this.resourceDatabase = resourceDatabase;
    }

    public static PermanentDatabase create(String originalResourceName) {
        ResourceDatabase original = ResourceDatabase.get(originalResourceName);
        return new PermanentDatabase(original);
    }

    public ResourceDatabase prepare() {
        File originalFile = resourceDatabase.getFile();
        String copyResourceName = originalFile.getName() + "-copy";

        File copyFile = new File(originalFile.getParentFile(), copyResourceName);
        InputStream in = resourceDatabase.getInputStream();

        try {
            OutputStream out = new FileOutputStream(copyFile);
            IOUtils.copy(in, out);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);

            ResourceDatabase resourceDatabase = ResourceDatabase.get(copyResourceName);
            return resourceDatabase;
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
