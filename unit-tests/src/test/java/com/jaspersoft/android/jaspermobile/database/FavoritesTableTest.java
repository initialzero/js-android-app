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

import android.database.Cursor;
import android.net.Uri;

import com.google.common.base.Preconditions;
import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.test.support.DatabaseSpecification;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesTableTest extends DatabaseSpecification {

    @Test
    public void testCascadeDelete() {
        ServerProfiles profile1 = new ServerProfiles();
        populateProfile(profile1);

        Uri profileUri1 = getContentResolver()
                .insert(MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile1.getContentValues());
        assertNewUri(profileUri1);
        long profile1_id = getIdFromUri(profileUri1);

        ServerProfiles profile2 = new ServerProfiles();
        populateProfile(profile2);

        Uri profileUri2 = getContentResolver()
                .insert(MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile2.getContentValues());
        assertNewUri(profileUri2);
        long profile2_id = getIdFromUri(profileUri2);

        createFavoriteInstances(profile1_id);
        createFavoriteInstances(profile2_id);

        int deleteRowCount = getContentResolver().delete(profileUri1, null, null);
        assertThat(deleteRowCount, is(1));

        Cursor cursor = queryFavoritesForProfile(profile1_id);
        Preconditions.checkNotNull(cursor);
        assertThat(cursor.getCount(), is(0));

        cursor = queryFavoritesForProfile(profile2_id);
        assertThat(cursor.getCount(), is(3));
    }

    private Cursor queryFavoritesForProfile(long profile_id) {
        return getContentResolver().query(MobileDbProvider.FAVORITES_CONTENT_URI,
                    new String[]{FavoritesTable._ID},
                    FavoritesTable.SERVER_PROFILE_ID + "=" + profile_id, null, null);
    }

    private void populateProfile(ServerProfiles profile) {
        profile.setVersioncode(5.0);
        profile.setEdition("PRO");
        profile.setServerUrl("http://some.url.com");
        profile.setOrganization("");
        profile.setPassword("1234");
    }

    private void createFavoriteInstances(long profileId) {
        Favorites favorite = new Favorites();
        for (int i = 0; i < 3; i++) {
            favorite.setServerProfileId(profileId);
            favorite.setName("Favorite " + i);
            Uri favoriteUri = getContentResolver()
                    .insert(MobileDbProvider.FAVORITES_CONTENT_URI, favorite.getContentValues());
            assertNewUri(favoriteUri);
        }
    }

    private void assertNewUri(Uri uri) {
        Preconditions.checkNotNull(uri);
        assertThat(Integer.valueOf(uri.getLastPathSegment()), greaterThan(0));
    }

    private long getIdFromUri(Uri uri) {
        return Long.valueOf(uri.getLastPathSegment());
    }

}
