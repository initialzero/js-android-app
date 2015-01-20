/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.profile.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.SingleChoiceSimpleCursorAdapter;
import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServersAdapter extends SingleChoiceSimpleCursorAdapter {
    private static final String[] FROM = {ServerProfilesTable.ALIAS, ServerProfilesTable.SERVER_URL, ServerProfilesTable._ID};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

    private ServersInteractionListener serversInteractionListener;

    public ServersAdapter(Context context, Bundle savedInstanceState, int layout) {
        super(savedInstanceState, context, layout, null, FROM, TO, 0);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.am_servers_menu, menu);
        return true;
    }

    public void setServersInteractionListener(ServersInteractionListener serversInteractionListener) {
        this.serversInteractionListener = serversInteractionListener;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Cursor cursor = getContext().getContentResolver()
                .query(MobileDbProvider.SERVER_PROFILES_CONTENT_URI,
                        new String[]{ServerProfilesTable._ID}, null, null, null);
        boolean hideDeleteControlCondition = cursor.getCount() > 1;
        menu.findItem(R.id.deleteItem).setVisible(hideDeleteControlCondition);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onEdit(getCurrentPosition());
                }
                break;
            case R.id.deleteItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onDelete(getCurrentPosition());
                }
                break;
            case R.id.cloneItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onClone(getCurrentPosition());
                }
                break;
            default:
                return false;
        }
        return false;
    }

    public static interface ServersInteractionListener {
        void onEdit(int position);

        void onDelete(int position);

        void onClone(int position);
    }
}
