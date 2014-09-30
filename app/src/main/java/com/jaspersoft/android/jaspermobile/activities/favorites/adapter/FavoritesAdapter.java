/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesAdapter extends SingleChoiceSimpleCursorAdapter {

    private static final String[] FROM = {FavoritesTable.LABEL, FavoritesTable.URI, FavoritesTable.WSTYPE};
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
        if (item.getItemId() == R.id.removeFromFavorites) {
            for (long id : getCheckedItems()) {
                Uri uri = Uri.withAppendedPath(JasperMobileProvider.FAVORITES_CONTENT_URI, String.valueOf(id));
                getContext().getContentResolver().delete(uri, null, null);
            }
            finishActionMode();
            return true;
        }
        return false;
    }

}
