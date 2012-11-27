/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util;

import roboguice.util.Ln;

import java.io.File;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public final class FileUtils {

    // This class cannot be instantiated
    private FileUtils() {}

    /**
     * Recursively delete files in the specified folder.
     * @param directory directory file
     * @return {@code true} if these files were deleted, {@code false} otherwise.
     */
    public static boolean deleteFilesInDirectory(File directory) {
        if(directory.exists()) {
            File[] childFiles = directory.listFiles();
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        deleteFilesInDirectory(childFile);
                    }
                    if (!childFile.delete()){
                        Ln.e("Unable to delete %s", childFile);
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
