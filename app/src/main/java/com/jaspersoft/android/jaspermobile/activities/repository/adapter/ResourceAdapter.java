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

package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.SingleChoiceAdapterHelper;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper_;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceAdapter extends SingleChoiceArrayAdapter<ResourceLookup> {
    private final FavoritesHelper_ favoriteHelper;
    private ResourceViewHelper viewHelper = new ResourceViewHelper();

    private final ViewType mViewType;
    private MenuItem favoriteActionItem;

    public static Builder builder(Context context, Bundle savedInstanceState) {
        checkNotNull(context);
        return new Builder(context, savedInstanceState);
    }

    private ResourceAdapter(Context context, Bundle savedInstanceState, ViewType viewType) {
        super(savedInstanceState, context, 0);
        favoriteHelper = FavoritesHelper_.getInstance_(context);
        mViewType = checkNotNull(viewType, "ViewType can`t be null");
    }

    @Override
    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        IResourceView itemView = (IResourceView) convertView;

        if (itemView == null) {
            if (mViewType == ViewType.LIST) {
                itemView = ListItemView_.build(getContext());
            } else {
                itemView = GridItemView_.build(getContext());
            }
        }

        viewHelper.populateView(itemView, getItem(position));
        return (View) itemView;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.am_resource_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        favoriteActionItem = menu.findItem(R.id.favoriteAction);
        if (getCount() > 0) {
            alterFavoriteIcon();
            return true;
        }
        return false;
    }

    @Override
    public void addAll(Collection<? extends ResourceLookup> collection) {
        super.addAll(collection);
        // Because of rotation we are loosing content of adapter. For that
        // reason we are altering ActionMode icon if it visible state to
        // the required value.
        if (favoriteActionItem != null && collection.size() > 0
                && getCurrentPosition() != SingleChoiceAdapterHelper.NO_POSITION) {
            alterFavoriteIcon();
        }
    }

    @Override
    public void clear() {
        super.clear();
        resetCurrentPosition();
    }

    private void alterFavoriteIcon() {
        ResourceLookup resource = getItem(getCurrentPosition());
        Cursor cursor = favoriteHelper.queryFavoriteByResource(resource);

        try {
            boolean alreadyFavorite = (cursor.getCount() > 0);
            favoriteActionItem.setIcon(alreadyFavorite ? R.drawable.ic_rating_favorite : R.drawable.ic_rating_not_favorite);
            favoriteActionItem.setTitle(alreadyFavorite ? R.string.r_cm_remove_from_favorites : R.string.r_cm_add_to_favorites);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ResourceLookup resource = getItem(getCurrentPosition());
        Uri uri = favoriteHelper.queryFavoriteUri(resource);
        favoriteHelper.handleFavoriteMenuAction(uri, resource, null);
        mode.invalidate();
        return true;
    }

    public static class Builder {
        private final Context context;
        private final Bundle savedInstanceState;

        private ViewType viewType;

        public Builder(Context context, Bundle savedInstanceState) {
            this.context = context;
            this.savedInstanceState = savedInstanceState;
        }

        public Builder setViewType(ViewType viewType) {
            this.viewType = viewType;
            return this;
        }

        public ResourceAdapter create() {
            return new ResourceAdapter(context, savedInstanceState, viewType);
        }
    }
}
