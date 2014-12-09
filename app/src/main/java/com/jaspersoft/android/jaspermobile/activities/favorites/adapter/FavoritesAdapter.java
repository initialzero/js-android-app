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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesAdapter extends SingleChoiceSimpleCursorAdapter {

    private static final String[] FROM = {FavoritesTable.TITLE, FavoritesTable.URI, FavoritesTable.WSTYPE};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

    public FavoritesAdapter(Context context, Bundle savedInstanceState, int layout) {
        super(savedInstanceState, context, layout, null, FROM, TO, 0);
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
        long id = Lists.newArrayList(getCheckedItems()).get(0);
        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                String.valueOf(id));

        switch (item.getItemId()) {
            case R.id.removeFromFavorites:
                getContext().getContentResolver().delete(uri, null, null);
                finishActionMode();
                return true;
            case R.id.showAction:
                showAboutInfo(uri);
                return true;
            default:
                return false;
        }
    }

    private void showAboutInfo(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri,
                new String[]{FavoritesTable.TITLE, FavoritesTable.DESCRIPTION}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();

                    String title = cursor.getString(cursor.getColumnIndex(FavoritesTable.TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(FavoritesTable.DESCRIPTION));
                    FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
                    SimpleDialogFragment.createBuilder(getContext(), fm)
                            .setTitle(title)
                            .setMessage(description)
                            .setNegativeButtonText(android.R.string.ok)
                            .show();
                }
            } finally {
                cursor.close();
            }
        }
    }

}
