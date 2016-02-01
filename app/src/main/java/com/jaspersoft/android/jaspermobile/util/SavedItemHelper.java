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

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EBean
public class SavedItemHelper {
    @RootContext
    protected Context context;

    /**
     * Removes item both from file system and database
     *
     * @param reportFile saved item file. It can be PDF, HTML, XLS
     * @param reportId   id of saved item inside saved_items table
     */
    public void deleteSavedItem(File reportFile, long reportId) {
        File reportFolderFile = reportFile.getParentFile();
        if (reportFolderFile.isDirectory()) {
            boolean reportFolderDeleted = deleteReportFolder(reportFolderFile);
            if (reportFolderDeleted) {
                deleteReferenceInDb(reportId);
            }
        }
    }

    public void deleteSavedItem(File reportFile, Uri reportUri) {
        File reportFolderFile = reportFile.getParentFile();
        if (reportFolderFile.isDirectory()) {
            boolean reportFolderDeleted = deleteReportFolder(reportFolderFile);
            if (reportFolderDeleted) {
                deleteReferenceInDb(reportUri);
            }
        }
    }

    public void deleteUnsavedItems() {
        String selection = SavedItemsTable.DOWNLOADED + " =?";
        Cursor cursor = context.getContentResolver().query(MobileDbProvider.SAVED_ITEMS_CONTENT_URI, new String[]{SavedItemsTable._ID, SavedItemsTable.FILE_PATH}, selection, new String[]{"0"}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(SavedItemsTable._ID));
                    File file = new File(cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH)));
                    deleteSavedItem(file, id);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
    }

    public boolean itemExist(String name, String format) {
        String selection = SavedItemsTable.NAME + " =? AND " + SavedItemsTable.FILE_FORMAT + " =?";
        Cursor cursor = context.getContentResolver().query(MobileDbProvider.SAVED_ITEMS_CONTENT_URI, new String[]{SavedItemsTable._ID}, selection, new String[]{name, format}, null);
        boolean itemExist = cursor != null && cursor.getCount() != 0;
        if (cursor != null) {
            cursor.close();
        }
        return itemExist;
    }

    private boolean deleteReportFolder(File reportFolderFile) {
        try {
            FileUtils.deleteDirectory(reportFolderFile);
            return true;
        } catch (IOException e) {
            Timber.e(e.getMessage(), "Failed to delete folder. Path: " + reportFolderFile.getPath());
            Toast.makeText(context, R.string.sdr_t_report_deletion_error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void deleteReferenceInDb(long reportId) {
        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                String.valueOf(reportId));
        deleteReferenceInDb(uri);
    }

    private void deleteReferenceInDb(Uri reportUri) {
        context.getContentResolver().delete(reportUri, null, null);
    }
}
