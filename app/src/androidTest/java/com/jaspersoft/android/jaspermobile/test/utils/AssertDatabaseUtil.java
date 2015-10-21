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

package com.jaspersoft.android.jaspermobile.test.utils;

import android.content.ContentResolver;
import android.database.Cursor;

import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class AssertDatabaseUtil {

    private AssertDatabaseUtil() {
        throw new AssertionError();
    }

    public static boolean containsSavedReport(ContentResolver contentResolver, String reportName, String fileFormat) {
        Cursor cursor = contentResolver.query(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                SavedItemsTable.ALL_COLUMNS, null, null, null);
        while (cursor.moveToNext()) {
            String currentReportName = cursor.getString(cursor.getColumnIndex(SavedItemsTable.NAME));
            String currentFileFormat = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));
            if(reportName.equals(currentReportName) && fileFormat.equals(currentFileFormat)) return true;
        }
        return false;
    }

}
