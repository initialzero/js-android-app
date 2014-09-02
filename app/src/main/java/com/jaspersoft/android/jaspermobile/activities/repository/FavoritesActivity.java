/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.ui.adapters.ResourceLookupArrayAdapter;
import com.jaspersoft.android.sdk.ui.adapters.ResourceLookupComparator;

import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Oleg Gavavka
 * @author Ivan Gadzhega
 * @since 1.0
 */

public class FavoritesActivity extends BaseRepositoryActivity {

    // Context menu IDs
    protected static final int ID_CM_REMOVE_FROM_FAVORITES = 26;

    private long serverProfileId;
    private String userName;
    private String organization;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serverProfileId = jsRestClient.getServerProfile().getId();
        userName = jsRestClient.getServerProfile().getUsername();
        organization = jsRestClient.getServerProfile().getOrganization();

        //update title
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString(EXTRA_BC_TITLE_LARGE);
            getActionBar().setTitle(title);
        }

        List<ResourceLookup> resourceLookups = new ArrayList<ResourceLookup>();

        Cursor cursor = dbProvider.fetchFavoriteItemsByParams(serverProfileId, userName, organization);
        if (cursor != null) {
            startManagingCursor(cursor);

            // Iterate DB Records
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ResourceLookup resource = new ResourceLookup();
                resource.setLabel(cursor.getString(cursor.getColumnIndex(Favorites.KEY_TITLE)));
                resource.setUri(cursor.getString(cursor.getColumnIndex(Favorites.KEY_URI)));
                resource.setDescription(cursor.getString(cursor.getColumnIndex(Favorites.KEY_DESCRIPTION)));
                resource.setResourceType(ResourceType.valueOf(cursor.getString(cursor.getColumnIndex(Favorites.KEY_WSTYPE))));
                resourceLookups.add(resource);
                cursor.moveToNext();
            }
        }

        // show the favorite resources
        if (resourceLookups.isEmpty()) {
            nothingToDisplayText.setText(R.string.r_browser_nothing_to_display);
        } else {
            ResourceLookupArrayAdapter arrayAdapter = new ResourceLookupArrayAdapter(this, resourceLookups);
            arrayAdapter.sort(new ResourceLookupComparator()); // sort: non-case-sensitive, folders first
            setListAdapter(arrayAdapter);
        }
    }

    //---------------------------------------------------------------------
    // Context menu
    //---------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        // hide "add to favorites" option
        menu.findItem(ID_CM_ADD_TO_FAVORITES).setVisible(false);
        // add "remove from favorites" option
        menu.add(Menu.NONE, ID_CM_REMOVE_FROM_FAVORITES, Menu.CATEGORY_SECONDARY, R.string.r_cm_remove_from_favorites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ResourceLookup resource = (ResourceLookup) getListView().getItemAtPosition(info.position);
        switch (item.getItemId()) {
            case ID_CM_REMOVE_FROM_FAVORITES:
                //Remove favorite item by uri, serverProfileId, username, organization
                dbProvider.deleteFavoriteItems(resource.getUri(), serverProfileId, userName, organization);
                ((ResourceLookupArrayAdapter) getListAdapter()).remove(resource);
                ((ResourceLookupArrayAdapter) getListAdapter()).notifyDataSetChanged();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
    }

}