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

import android.content.Intent;
import android.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.5
 */
public abstract class BaseBrowserSearchActivity extends  BaseRepositoryActivity {

    // Action Bar IDs
    protected static final int ID_AB_FAVORITES = 31;
    protected static final int ID_AB_REFRESH = 32;
    protected static final int ID_AB_SEARCH = 33;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add actions to the action bar
        menu.add(Menu.NONE, ID_AB_FAVORITES, Menu.NONE, R.string.r_ab_favorites)
                .setIcon(R.drawable.ic_action_favorites).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, ID_AB_SEARCH, Menu.NONE, R.string.r_ab_search)
                .setIcon(R.drawable.ic_action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, ID_AB_REFRESH, Menu.NONE, R.string.r_ab_refresh)
                .setIcon(R.drawable.ic_action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_FAVORITES:
                Intent favoritesIntent = new Intent();
                favoritesIntent.setClass(this, FavoritesActivity.class);
                favoritesIntent.putExtra(FavoritesActivity.EXTRA_BC_TITLE_LARGE, getString(R.string.f_title));
                startActivity(favoritesIntent);
                return true;
            case ID_AB_SEARCH:
                onSearchRequested();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

}
