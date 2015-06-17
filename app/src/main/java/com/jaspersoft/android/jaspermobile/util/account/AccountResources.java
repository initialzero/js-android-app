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

package com.jaspersoft.android.jaspermobile.util.account;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.migrate.SavedItemsMigration;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Utility responsible for flushing resources like favorites and saved items associated with deleted account.
 * This class will query accounts for changes except 'com.jaspersoft.account.none' which is reserved for internal use.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountResources {
    private final Context context;
    private final String[] accountNames;

    private AccountResources(Context context) {
        this.context = context;
        this.accountNames = prepareAccountNames(context);
        Timber.tag(AccountResources.class.getSimpleName());
    }

    private String[] prepareAccountNames(Context context) {
        Account[] accounts = JasperAccountManager.get(context).getAccounts();
        int count = accounts.length;
        String[] accountNames = new String[count + 1];
        for (int i = 0; i < count; i++) {
            accountNames[i] = accounts[i].name;
        }
        // Manually add internal account
        accountNames[count] = SavedItemsMigration.SHARED_DIR;
        return accountNames;
    }

    public static AccountResources get(Context context) {
        return new AccountResources(context);
    }

    public void flushOnDemand() {
        flushFavorites();
        flushSavedItems();
    }

    private void flushFavorites() {
        context.getContentResolver().delete(
                MobileDbProvider.FAVORITES_CONTENT_URI,
                FavoritesTable.ACCOUNT_NAME + " NOT IN (" + makePlaceholders(accountNames.length) + ")",
                accountNames);
    }

    private void flushSavedItems() {
        Cursor cursor = context.getContentResolver().query(
                MobileDbProvider.SAVED_ITEMS_CONTENT_URI, new String[]{SavedItemsTable._ID, SavedItemsTable.FILE_PATH},
                SavedItemsTable.ACCOUNT_NAME + " NOT IN (" + makePlaceholders(accountNames.length) + " )", accountNames, null);
        File file;
        Uri uriForDelete;
        String filePath, id;
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        try {
            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(SavedItemsTable._ID));
                filePath = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH));
                file = new File(filePath);
                if (file.delete()) {
                    uriForDelete = Uri.withAppendedPath(MobileDbProvider.SAVED_ITEMS_CONTENT_URI, id);
                    batch.add(ContentProviderOperation.newDelete(uriForDelete).build());
                }
            }
        } finally {
            cursor.close();
        }

        try {
            context.getContentResolver().applyBatch(MobileDbProvider.AUTHORITY, batch);
        } catch (RemoteException e) {
            Timber.e(e.getMessage(), e);
        } catch (OperationApplicationException e) {
            Timber.e(e.getMessage(), e);
        }
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}
