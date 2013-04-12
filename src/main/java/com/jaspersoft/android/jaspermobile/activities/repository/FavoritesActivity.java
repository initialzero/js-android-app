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

package com.jaspersoft.android.jaspermobile.activities.repository;

import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.tables.Favorites;
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorArrayAdapter;
import com.jaspersoft.android.sdk.ui.adapters.ResourceDescriptorComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Gavavka
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */

public class FavoritesActivity extends BaseRepositoryActivity{

    // Context menu IDs
    protected static final int ID_CM_REMOVE_FROM_FAVORITES = 26;

    private long serverProfileId;
    private String userName;
    private String organization;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide unusable action buttons
        favoriteButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);

        serverProfileId = jsRestClient.getServerProfile().getId();
        userName = jsRestClient.getServerProfile().getUsername();
        organization = jsRestClient.getServerProfile().getOrganization();

        //update bread crumbs
        Bundle extras = getIntent().getExtras();
        String titleLarge = extras.getString(EXTRA_BC_TITLE_LARGE);
        breadCrumbsTitleLarge.setText(titleLarge);

        Cursor cursor = dbProvider.fetchFavoriteItemsByParams(serverProfileId, userName, organization);
        startManagingCursor(cursor);
        List<ResourceDescriptor> resourceDescriptors = new ArrayList<ResourceDescriptor>();

        // Iterate DB Records
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ResourceDescriptor resource = new ResourceDescriptor();
            resource.setName(cursor.getString(cursor.getColumnIndex(Favorites.KEY_NAME)));
            resource.setLabel(cursor.getString(cursor.getColumnIndex(Favorites.KEY_TITLE)));
            resource.setUriString(cursor.getString(cursor.getColumnIndex(Favorites.KEY_URI)));
            resource.setDescription(cursor.getString(cursor.getColumnIndex(Favorites.KEY_DESCRIPTION)));
            resource.setWsType(ResourceDescriptor.WsType.valueOf(cursor.getString(cursor.getColumnIndex(Favorites.KEY_WSTYPE))));
            resourceDescriptors.add(resource);
            cursor.moveToNext();
        }

        // show the favorite resources
        if (resourceDescriptors.isEmpty()) {
            // Show text that there are no favorite resources
            nothingToDisplayText.setText(R.string.r_browser_nothing_to_display);
            nothingToDisplayText.setVisibility(View.VISIBLE);
        } else {
            nothingToDisplayText.setVisibility(View.GONE);
            ResourceDescriptorArrayAdapter arrayAdapter = new ResourceDescriptorArrayAdapter(this, resourceDescriptors);
            arrayAdapter.sort(new ResourceDescriptorComparator()); // sort: non-case-sensitive, folders firs
            setListAdapter(arrayAdapter);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        // hide "add to favorites" option
        menu.findItem(ID_CM_ADD_TO_FAVORITES).setVisible(false);
        // add "remove from favorites" option
        menu.add(Menu.NONE, ID_CM_REMOVE_FROM_FAVORITES, Menu.NONE, R.string.r_cm_remove_from_favorites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ResourceDescriptor resourceDescriptor = (ResourceDescriptor) getListView().getItemAtPosition(info.position);
        switch (item.getItemId()) {
            case ID_CM_REMOVE_FROM_FAVORITES:
                //Remove favorite item by uri, serverProfileId, username, organization
                dbProvider.deleteFavoriteItems(resourceDescriptor.getUriString(), serverProfileId, userName, organization);
                ((ResourceDescriptorArrayAdapter) getListAdapter()).remove(resourceDescriptor);
                ((ResourceDescriptorArrayAdapter) getListAdapter()).notifyDataSetChanged();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
    }

}
