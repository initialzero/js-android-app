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
import android.view.MenuItem;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.model.Favorites;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@PerProfile
public class FavoritesHelper {
    private final Context context;
    private final Analytics analytics;
    private final Profile profile;
    private final Toast toast;

    @Inject
    public FavoritesHelper(
            @ApplicationContext Context context,
            Analytics analytics,
            Profile profile
    ) {
        this.context = context;
        this.analytics = analytics;
        this.profile = profile;
        this.toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public void switchFavoriteState(ResourceLookup resource, MenuItem favoriteIcon) {
        boolean isAlreadyFavorite = isFavorite(resource.getUri());
        boolean changeStateSucceed;

        if (isAlreadyFavorite) {
            changeStateSucceed = removeFromFavorite(resource.getUri());
        } else {
            changeStateSucceed = addToFavorites(resource);
        }

        boolean newFavoriteState = !isAlreadyFavorite;
        showFavoriteStateChanged(newFavoriteState, changeStateSucceed);

        if (changeStateSucceed) {
            updateFavoriteIconState(favoriteIcon, newFavoriteState);
            analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.MARKED_AS_FAVORITE.getValue(), "" + newFavoriteState);
        }
    }

    public void updateFavoriteIconState(MenuItem favoriteAction, String resourceUri) {
        boolean isFavorite = isFavorite(resourceUri);
        updateFavoriteIconState(favoriteAction, isFavorite);
    }

    private boolean isFavorite(String resourceUri) {
        Cursor cursor = queryFavorite(resourceUri);
        boolean isFavorite = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }

        return isFavorite;
    }

    private Cursor queryFavorite(String resourceUri) {
        if (resourceUri == null || resourceUri.isEmpty()) return null;

        StringBuilder selection = new StringBuilder("");
        String[] selectionArgs = new String[2];

        //Add account name to WHERE params
        selection.append(FavoritesTable.ACCOUNT_NAME + " =?");
        selectionArgs[0] = profile.getKey();

        //Add and to WHERE params
        selection.append(" AND ");

        //Add resourceUri to WHERE params
        selection.append(FavoritesTable.URI + " =?");
        selectionArgs[1] = resourceUri;

        return context.getContentResolver().query(JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                new String[]{FavoritesTable._ID}, selection.toString(), selectionArgs, null);
    }

    private Uri getFavoriteUri(String resourceUri) {
        Uri favoriteEntryUri = null;
        Cursor cursor = queryFavorite(resourceUri);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToPosition(0);
                String id = cursor.getString(cursor.getColumnIndex(FavoritesTable._ID));
                favoriteEntryUri = Uri.withAppendedPath(JasperMobileDbProvider.FAVORITES_CONTENT_URI, id);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return favoriteEntryUri;
    }

    private boolean removeFromFavorite(String resourceUri) {
        int removedCount = context.getContentResolver().delete(getFavoriteUri(resourceUri), null, null);
        return removedCount > 0;
    }

    private boolean addToFavorites(ResourceLookup resource) {
        Favorites favoriteEntry = new Favorites();

        favoriteEntry.setUri(resource.getUri());
        favoriteEntry.setTitle(resource.getLabel());
        favoriteEntry.setDescription(resource.getDescription());
        favoriteEntry.setWstype(resource.getResourceType().toString());
        favoriteEntry.setAccountName(profile.getKey());
        favoriteEntry.setCreationTime(resource.getCreationDate());

        return context.getContentResolver().insert(JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                favoriteEntry.getContentValues()) != null;
    }

    private void showFavoriteStateChanged(boolean isFavorite, boolean succeed) {
        int messageId;

        if (isFavorite) {
            if (succeed) {
                messageId = R.string.r_cm_added_to_favorites;
            } else {
                messageId = R.string.r_cm_add_to_favorites_failed;
            }
        } else {
            if (succeed) {
                messageId = R.string.r_cm_removed_from_favorites;
            } else {
                messageId = R.string.r_cm_remove_from_favorites_failed;
            }
        }

        toast.setText(context.getString(messageId));
        toast.show();
    }

    public Toast getToast() {
        return toast;
    }

    private void updateFavoriteIconState(MenuItem favoriteAction, boolean isFavorite) {
        if (favoriteAction != null) {
            favoriteAction.setIcon(isFavorite ? R.drawable.ic_menu_star : R.drawable.ic_menu_star_outline);
            favoriteAction.setTitle(isFavorite ? R.string.r_cm_remove_from_favorites : R.string.r_cm_add_to_favorites);
        }
    }
}
