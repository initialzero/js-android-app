/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.favorites.fragment;

import android.app.ActionBar;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceViewHelper;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import javax.inject.Inject;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FavoritesFragment extends RoboFragment
        implements SimpleCursorAdapter.ViewBinder, AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    // Context menu action
    private static final int ID_CM_FAVORITE = 10;

    @FragmentArg
    ViewType viewType;

    @InjectView(android.R.id.list)
    AbsListView listView;
    @InjectView(android.R.id.empty)
    TextView emptyText;

    @Inject
    JsRestClient jsRestClient;

    @Bean
    ResourceOpener resourceOpener;

    private SimpleCursorAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                (viewType == ViewType.LIST) ? R.layout.common_list_layout : R.layout.common_grid_layout,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(0);

        registerForContextMenu(listView);

        String[] from = {FavoritesTable.LABEL, FavoritesTable.URI, FavoritesTable.WSTYPE};
        int[] to = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

        mAdapter = new SimpleCursorAdapter(getActivity(),
                (viewType == ViewType.LIST) ? R.layout.common_list_item : R.layout.common_grid_item,
                null, from, to, 0);
        mAdapter.setViewBinder(this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.f_title);
        }
    }

    //---------------------------------------------------------------------
    // Implements Context Menu
    //---------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        // Determine on which item in the ListView the user long-clicked
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(info.position);

        // Retrieve the label for that particular item and use it as title for the menu
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(FavoritesTable.LABEL)));

        // Add all the menu options
        menu.add(Menu.NONE, ID_CM_FAVORITE, Menu.NONE, R.string.r_cm_remove_from_favorites);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Determine on which item in the ListView the user long-clicked and get it from Cursor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Handle item selection
        switch (item.getItemId()) {
            case ID_CM_FAVORITE:
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(info.position);
                String id = cursor.getString(cursor.getColumnIndex(FavoritesTable._ID));
                Uri uri = Uri.withAppendedPath(JasperMobileProvider.FAVORITES_CONTENT_URI, id);
                getActivity().getContentResolver().delete(uri, null, null);
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Implements AbsListView.OnItemClickListener
    //---------------------------------------------------------------------

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(FavoritesTable.WSTYPE)) {
            String wsType = cursor.getString(columnIndex);
            ResourceType resourceType = ResourceType.valueOf(wsType);
            ImageView imageView = (ImageView) view;
            imageView.setImageResource(ResourceViewHelper.getResourceIcon(resourceType));
            return true;
        }
        return false;
    }

    //---------------------------------------------------------------------
    // Implements SimpleCursorAdapter.ViewBinder
    //---------------------------------------------------------------------

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(cursor.getString(cursor.getColumnIndex(FavoritesTable.LABEL)));
        resource.setUri(cursor.getString(cursor.getColumnIndex(FavoritesTable.URI)));
        resource.setResourceType(cursor.getString(cursor.getColumnIndex(FavoritesTable.WSTYPE)));

        resourceOpener.openResource(resource);
    }

    //---------------------------------------------------------------------
    // Implements LoaderManager.LoaderCallbacks<Cursor>
    //---------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection =
                FavoritesTable.SERVER_PROFILES_ID + " =?  AND " +
                        FavoritesTable.USERNAME + " =?  AND " +
                        FavoritesTable.ORGANIZATION + " =?";
        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        String[] selectionArgs = {
                String.valueOf(jsServerProfile.getId()),
                jsServerProfile.getUsername(),
                jsServerProfile.getOrganization()
        };
        return new CursorLoader(getActivity(), JasperMobileProvider.FAVORITES_CONTENT_URI,
                FavoritesTable.ALL_COLUMNS, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0) {
            setEmptyText(0);
        } else {
            setEmptyText(R.string.f_empty_list_msg);
        }
    }

    @UiThread
    protected void setEmptyText(int resId) {
        if (resId == 0) {
            emptyText.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(resId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

}
