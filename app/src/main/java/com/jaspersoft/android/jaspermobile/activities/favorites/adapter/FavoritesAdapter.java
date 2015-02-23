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
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.GridItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceViewHelper;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.util.multichoice.SingleChoiceCursorAdapter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FavoritesAdapter extends SingleChoiceCursorAdapter {

    private final ViewType mViewType;
    private final ResourceViewHelper mViewHelper;
    private FavoritesInteractionListener mFavoritesInteractionListener;

    public FavoritesAdapter(Context context, Bundle savedInstanceState, ViewType viewType) {
        super(savedInstanceState, context, null, 0);
        mViewType = viewType;
        mViewHelper = new ResourceViewHelper(context);
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

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View itemView;
        if (mViewType == ViewType.LIST) {
            itemView = ListItemView_.build(getContext());
        } else {
            itemView = GridItemView_.build(getContext());
        }
        return itemView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ResourceView itemView = (ResourceView) view;
        mViewHelper.populateView(itemView, transformCursor(cursor));
    }

    private ResourceLookup transformCursor(Cursor cursor) {
        ResourceLookup resourceLookup = new ResourceLookup();
        resourceLookup.setLabel(cursor.getString(cursor.getColumnIndex(FavoritesTable.TITLE)));
        resourceLookup.setUri(cursor.getString(cursor.getColumnIndex(FavoritesTable.URI)));
        resourceLookup.setResourceType(cursor.getString(cursor.getColumnIndex(FavoritesTable.WSTYPE)));
        resourceLookup.setCreationDate(cursor.getString(cursor.getColumnIndex(FavoritesTable.CREATION_TIME)));
        return resourceLookup;
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public static interface FavoritesInteractionListener {
        void onDelete(String title, Uri itemToDelete);
        void onInfo(String title, String description);
    }

}
