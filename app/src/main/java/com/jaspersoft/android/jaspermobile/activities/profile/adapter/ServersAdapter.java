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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.SingleChoiceSimpleCursorAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.IResourceView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServersAdapter extends SingleChoiceSimpleCursorAdapter
        implements AdapterView.OnItemClickListener {
    private static final String[] FROM = {ServerProfilesTable.ALIAS, ServerProfilesTable.SERVER_URL, ServerProfilesTable._ID};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

    private ServersInteractionListener serversInteractionListener;
    private long mServerProfileId;

    public ServersAdapter(Context context, Bundle savedInstanceState, long serverProfileId) {
        super(savedInstanceState, context, R.layout.common_list_item, null, FROM, TO, 0);
        mServerProfileId = serverProfileId;
        setOnItemClickListener(this);
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
    public View getViewImpl(int position, View convertView, ViewGroup parent) {
        IResourceView itemView = (IResourceView) convertView;

        if (itemView == null) {
            itemView = ListItemView_.build(getContext());
        }

        Cursor cursor = getCursor();
        cursor.moveToPosition(position);

        String alias = cursor.getString(cursor.getColumnIndex(ServerProfilesTable.ALIAS));
        String serverUrl = cursor.getString(cursor.getColumnIndex(ServerProfilesTable.SERVER_URL));

        boolean activeProfileExist = mServerProfileId != -1;
        int serverImageResource = R.drawable.ic_composed_server;

        if (activeProfileExist) {
            long entryId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));
            boolean isItemActive = (mServerProfileId == entryId);

            alias = isItemActive ? getContext().getString(R.string.sp_active_item, alias) : alias;
            serverImageResource = isItemActive ? R.drawable.ic_composed_active_server : R.drawable.ic_composed_server;
        }

        itemView.getImageView().setImageResource(serverImageResource);
        itemView.setTitle(alias);
        itemView.setSubTitle(serverUrl);

        return (View) itemView;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(getCurrentPosition());
        long profileId = cursor.getLong(cursor.getColumnIndex(ServerProfilesTable._ID));

        switch (item.getItemId()) {
            case R.id.editItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onEdit(profileId);
                }
                break;
            case R.id.deleteItem:
                if (serversInteractionListener != null) {
                    if(profileId == mServerProfileId) profileId = -1;
                    serversInteractionListener.onDelete(profileId);
                }
                break;
            case R.id.cloneItem:
                if (serversInteractionListener != null) {
                    serversInteractionListener.onClone(profileId);
                }
                break;
            default:
                return false;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (serversInteractionListener != null) {
            serversInteractionListener.onSelect(position);
        }
    }

    public static interface ServersInteractionListener {
        void onSelect(int position);

        void onEdit(long profileId);

        void onDelete(long profileId);

        void onClone(long profileId);
    }
}
