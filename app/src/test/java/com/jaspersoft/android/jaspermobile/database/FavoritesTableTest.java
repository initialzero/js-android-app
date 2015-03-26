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

package com.jaspersoft.android.jaspermobile.database;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.test.support.CustomRobolectricTestRunner;
import com.jaspersoft.android.jaspermobile.test.support.DatabaseRule;
import com.jaspersoft.android.jaspermobile.test.support.shadows.CustomShadowApplication;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.Random;

import static com.jaspersoft.android.jaspermobile.test.support.JsAssertions.assertNewUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(
        manifest = "app/src/main/AndroidManifest.xml",
        emulateSdk = 18,
        shadows = {CustomShadowApplication.class}
)
public class FavoritesTableTest {
    @Rule
    public final DatabaseRule databaseRule = new DatabaseRule();

    @Ignore("Scheduled for later task")
    @Test
    public void accountDeleteShouldTriggereCascadeDelete() {
        Account account1 = createAccount();
        Account account2 = createAccount();

        createFavoriteInstances(account1);
        createFavoriteInstances(account2);

        // TODO implement test case
        // Delete first account

        Cursor cursor = queryFavoritesForAccount(account1);
        assertThat(cursor, notNullValue());
        assertThat(cursor.getCount(), is(0));

        cursor = queryFavoritesForAccount(account2);
        assertThat(cursor.getCount(), is(3));
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private Cursor queryFavoritesForAccount(Account account) {
        return getContentResolver().query(MobileDbProvider.FAVORITES_CONTENT_URI,
                new String[]{FavoritesTable._ID},
                FavoritesTable.ACCOUNT_NAME + "=?", new String[]{account.name}, null);
    }

    private Account createAccount() {
        return new Account("Alias " + new Random().nextInt(), "com.test");
    }

    private void createFavoriteInstances(Account account) {
        Favorites favorite = new Favorites();
        for (int i = 0; i < 3; i++) {
            favorite.setAccountName(account.name);
            favorite.setTitle("Favorite " + i);
            Uri favoriteUri = getContentResolver()
                    .insert(MobileDbProvider.FAVORITES_CONTENT_URI, favorite.getContentValues());
            assertNewUri(favoriteUri);
        }
    }

    private static long getIdFromUri(Uri uri) {
        return Long.valueOf(uri.getLastPathSegment());
    }

    private Context getContext() {
        return Robolectric.application;
    }

    private ContentResolver getContentResolver() {
        return Robolectric.application.getContentResolver();
    }

}
