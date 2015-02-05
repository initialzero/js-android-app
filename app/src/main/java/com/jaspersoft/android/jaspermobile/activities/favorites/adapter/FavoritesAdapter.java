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

package com.jaspersoft.android.jaspermobile.activities.favorites.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesAdapter extends SingleChoiceSimpleCursorAdapter {

    private static final String[] FROM = {FavoritesTable.TITLE, FavoritesTable.URI, FavoritesTable.WSTYPE};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

    private FavoritesInteractionListener mFavoritesInteractionListener;

    public FavoritesAdapter(Context context, Bundle savedInstanceState, int layout) {
        super(savedInstanceState, context, layout, null, FROM, TO, 0);
    }

    public void setFavoritesInteractionListener(FavoritesInteractionListener favoritesInteractionListener) {
        mFavoritesInteractionListener = favoritesInteractionListener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.am_favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle(R.string.r_cm_remove_from_favorites);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(getCurrentPosition());
        String title = cursor.getString(cursor.getColumnIndex(FavoritesTable.TITLE));

        switch (item.getItemId()) {
            case R.id.removeFromFavorites:
                if (mFavoritesInteractionListener != null) {
                    long itemId = cursor.getLong(cursor.getColumnIndex(FavoritesTable._ID));
                    Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                            String.valueOf(itemId));
                    mFavoritesInteractionListener.onDelete(title, uri);
                }
                return true;
            case R.id.showAction:
                if (mFavoritesInteractionListener != null) {
                    String description = cursor.getString(cursor.getColumnIndex(FavoritesTable.DESCRIPTION));
                    mFavoritesInteractionListener.onInfo(title, description);
                }
                return true;
            default:
                return false;
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public static interface FavoritesInteractionListener {
        void onDelete(String title, Uri itemToDelete);

        void onInfo(String title, String description);
    }

}
