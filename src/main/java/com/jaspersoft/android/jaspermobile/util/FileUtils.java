/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile.util;

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
     */
    public static void deleteFilesInDirectory(File directory) {
        if(directory.exists()) {
            File[] childFiles = directory.listFiles();
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        deleteFilesInDirectory(childFile);
                        childFile.delete();
                    } else {
                        childFile.delete();
                    }
                }
            }
        }
    }
}
