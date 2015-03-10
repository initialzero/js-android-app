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
