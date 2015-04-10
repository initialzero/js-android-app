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

package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper_;
import com.jaspersoft.android.jaspermobile.util.multichoice.SingleChoiceAdapterHelper;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceViewHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.Collection;
import java.util.Comparator;


public class ResourceAdapter extends SingleChoiceArrayAdapter<ResourceAdapter.KpiResourceLookup> {
    private final FavoritesHelper_ favoriteHelper;
    private final ResourceViewHelper viewHelper;

    private final ViewType mViewType;
    private MenuItem favoriteActionItem;
    private ResourceInteractionListener mResourceInteractionListener;

    public ResourceAdapter(Context context,
                            Bundle savedInstanceState, ViewType viewType) {
        super(savedInstanceState, context, 0);
        favoriteHelper = FavoritesHelper_.getInstance_(context);
        if (viewType == null) {
            throw new IllegalArgumentException("ViewType can`t be null");
        }
        mViewType = viewType;
        viewHelper = new ResourceViewHelper(context);
    }

    public void setResourcesInteractionListener(ResourceInteractionListener resourceInteractionListener) {
        mResourceInteractionListener = resourceInteractionListener;
    }

    @Override
    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        ResourceView itemView = (ResourceView) convertView;

        if (itemView == null) {
            if (mViewType == ViewType.LIST) {
                itemView = ListItemView_.build(getContext());
            } else {
                itemView = GridItemView_.build(getContext());
            }
        }

        viewHelper.populateView(itemView, getItem(position).getResource());
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
    public void addAll(Collection<? extends KpiResourceLookup> collection) {
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
        ResourceLookup resource = getItem(getCurrentPosition()).getResource();
        Cursor cursor = favoriteHelper.queryFavoriteByResource(resource);

        try {
            boolean alreadyFavorite = (cursor.getCount() > 0);
            favoriteActionItem.setIcon(alreadyFavorite ? R.drawable.ic_menu_star : R.drawable.ic_menu_star_outline);
            favoriteActionItem.setTitle(alreadyFavorite ? R.string.r_cm_remove_from_favorites : R.string.r_cm_add_to_favorites);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ResourceLookup resource = getItem(getCurrentPosition()).getResource();
        switch (item.getItemId()) {
            case R.id.favoriteAction:
                if(mResourceInteractionListener != null) {
                    mResourceInteractionListener.onFavorite(resource);
                }
                break;
            case R.id.showAction:
                if(mResourceInteractionListener != null) {
                    mResourceInteractionListener.onInfo(resource.getLabel(), resource.getDescription());
                }
                break;
        }
        mode.invalidate();
        return true;
    }

    public void sortByType() {
        super.sort(new OrderingByType());
    }

    private static class OrderingByType implements Comparator<KpiResourceLookup> {
        @Override
        public int compare(KpiResourceLookup res1, KpiResourceLookup res2) {
            ResourceLookup.ResourceType resType1 = res1.getResource().getResourceType();
            ResourceLookup.ResourceType resType2 = res2.getResource().getResourceType();
            return compare(resType1.ordinal(), resType2.ordinal());
        }

        private static int compare(int lhs, int rhs) {
            return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public static interface ResourceInteractionListener {
        void onFavorite(ResourceLookup resource);
        void onInfo(String temTitle, String itemDescription);
    }

    public static class KpiResourceLookup {
        private final String kpiUri;
        private final ResourceLookup resource;

        public KpiResourceLookup(String kpiUri, ResourceLookup resource) {
            this.kpiUri = kpiUri;
            this.resource = resource;
        }

        public String getKpiUri() {
            return kpiUri;
        }

        public ResourceLookup getResource() {
            return resource;
        }
    }
}
