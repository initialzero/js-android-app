/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.integration;

import android.accounts.Account;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.test.utils.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.utils.SavedFilesUtil;
import com.jaspersoft.android.jaspermobile.util.account.AccountResources;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparisons.greaterThan;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class AccountResourcesTest extends AndroidTestCase {

    private Account account1;
    private Account account2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AccountUtil accountUtil = AccountUtil.get(getContext());
        accountUtil.removeAllAccounts();

        AccountServerData serverData = new AccountServerData();
        serverData.setAlias("Account 1");
        account1 = accountUtil.addAccount(serverData).getAccount();

        serverData = new AccountServerData();
        serverData.setAlias("Account 2");
        account2 = accountUtil.addAccount(serverData).getAccount();
    }

    public void testAssertAccountRemovalFlushesAssociatedFavorites() {
        getContext().getContentResolver().delete(MobileDbProvider.FAVORITES_CONTENT_URI, null, null);
        createFavoriteInstances(account1);
        createFavoriteInstances(account2);

        Cursor cursor = queryFavoritesForAccount(account1);
        assertThat(cursor.getCount(), is(3));
        cursor.close();

        AccountUtil.get(getContext()).removeAccount(account1);
        AccountResources.get(getContext()).flushOnDemand();
        cursor = queryFavoritesForAccount(account1);
        assertThat(cursor.getCount(), is(0));
        cursor.close();

        cursor = queryFavoritesForAccount(account2);
        assertThat(cursor.getCount(), is(3));
        cursor.close();
    }

    public void testAssertAccountRemovalFlushesAssociatedSavedItems() throws IOException {
        SavedFilesUtil.clear(getContext());
        getContext().getContentResolver().delete(MobileDbProvider.SAVED_ITEMS_CONTENT_URI, null, null);

        createSavedItemsInstances(account1);
        createSavedItemsInstances(account2);

        Cursor cursor = querySavedItemsForAccount(account1);
        assertThat(cursor.getCount(), is(3));
        cursor.close();

        AccountUtil.get(getContext()).removeAccount(account1);
        AccountResources.get(getContext()).flushOnDemand();
        cursor = querySavedItemsForAccount(account1);
        assertThat(cursor.getCount(), is(0));
        cursor.close();

        cursor = querySavedItemsForAccount(account2);
        assertThat(cursor.getCount(), is(3));
        cursor.close();
    }

    private Cursor queryFavoritesForAccount(Account account) {
        return getContext().getContentResolver().query(MobileDbProvider.FAVORITES_CONTENT_URI,
                new String[]{FavoritesTable._ID},
                FavoritesTable.ACCOUNT_NAME + " =?", new String[] {account.name}, null);
    }

    private Cursor querySavedItemsForAccount(Account account) {
        return getContext().getContentResolver().query(MobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                new String[]{SavedItemsTable._ID},
                SavedItemsTable.ACCOUNT_NAME + " =?", new String[] {account.name}, null);
    }

    private void createFavoriteInstances(Account account) {
        Favorites favorite = new Favorites();
        for (int i = 0; i < 3; i++) {
            favorite.setTitle("Favorite " + new Random().nextInt());
            favorite.setAccountName(account.name);
            Uri uri = getContext().getContentResolver()
                    .insert(MobileDbProvider.FAVORITES_CONTENT_URI, favorite.getContentValues());
            assertThat(uri, notNullValue());
            assertThat(Long.valueOf(uri.getLastPathSegment()), greaterThan(0L));
        }
    }

    private void createSavedItemsInstances(Account account) throws IOException {
        SavedItems savedItems = new SavedItems();
        File file;
        File parent = SavedFilesUtil.getSavedReportsDirectory(getContext());
        if (!parent.exists()) {
            assertThat(parent.createNewFile(), is(true));
        }

        for (int i = 0; i < 3; i++) {
            int random = new Random().nextInt();
            file = new File(parent, "file " + random + ".txt");
            assertThat(file.createNewFile(), is(true));

            savedItems.setName("Saved items " + random);
            savedItems.setAccountName(account.name);
            savedItems.setFilePath(file.getPath());

            Uri uri = getContext().getContentResolver()
                    .insert(MobileDbProvider.SAVED_ITEMS_CONTENT_URI, savedItems.getContentValues());
            assertThat(uri, notNullValue());
            assertThat(Long.valueOf(uri.getLastPathSegment()), greaterThan(0L));
        }
    }

}
