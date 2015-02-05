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

package com.jaspersoft.android.jaspermobile.activities.favorites.fragment;

import android.app.ActionBar;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.FavoritesAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceViewHelper;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;

import javax.inject.Inject;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FavoritesFragment extends RoboFragment
        implements SimpleCursorAdapter.ViewBinder, LoaderManager.LoaderCallbacks<Cursor>, FavoritesAdapter.FavoritesInteractionListener {

    private final int FAVORITES_LOADER_ID = 0;

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

    @InstanceState
    ResourceType filterType;

    @FragmentArg
    @InstanceState
    String searchQuery;

    @InstanceState
    SortOrder sortOrder;

    private FavoritesAdapter mAdapter;

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

        int layout = (viewType == ViewType.LIST) ? R.layout.common_list_item : R.layout.common_grid_item;
        mAdapter = new FavoritesAdapter(getActivity(), savedInstanceState, layout);
        mAdapter.setAdapterView(listView);
        mAdapter.setViewBinder(this);
        mAdapter.setFavoritesInteractionListener(this);
        listView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(searchQuery == null ? getString(R.string.f_title) : getString(R.string.search_result_format, searchQuery));
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
        super.onSaveInstanceState(outState);
    }

    @ItemClick(android.R.id.list)
    final void itemClick(int position) {
        mAdapter.finishActionMode();
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(cursor.getString(cursor.getColumnIndex(FavoritesTable.TITLE)));
        resource.setDescription(cursor.getString(cursor.getColumnIndex(FavoritesTable.DESCRIPTION)));
        resource.setUri(cursor.getString(cursor.getColumnIndex(FavoritesTable.URI)));
        resource.setResourceType(cursor.getString(cursor.getColumnIndex(FavoritesTable.WSTYPE)));

        resourceOpener.openResource(resource);
    }

    public void showSavedItemsByFilter(ResourceType selectedFilter) {
        filterType = selectedFilter;
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    public void showSavedItemsBySortOrder(SortOrder selectedSortOrder) {
        sortOrder = selectedSortOrder;
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
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

    //---------------------------------------------------------------------
    // Implements SimpleCursorAdapter.ViewBinder
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
    // Implements LoaderManager.LoaderCallbacks<Cursor>
    //---------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        StringBuilder selection = new StringBuilder("");
        ArrayList<String> selectionArgs = Lists.newArrayList();
        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        boolean noOrganization = jsServerProfile.getOrganization() == null;

        //Add server profile id and username to WHERE params
        selection.append(FavoritesTable.ACCOUNT_NAME + " =?");
        selectionArgs.add(BasicAccountProvider.get(getActivity()).getAccount().name);

        //Add organization to WHERE params
        if (noOrganization) {
            selection.append("  AND ")
                    .append(FavoritesTable.ORGANIZATION + " IS NULL");
        } else {
            selection.append("  AND ")
                    .append(FavoritesTable.ORGANIZATION + " =?");
            selectionArgs.add(String.valueOf(jsServerProfile.getOrganization()));
        }

        //Add filtration to WHERE params
        boolean withFiltering = filterType != null;
        if (withFiltering) {
            selection.append(" AND ")
                    .append(FavoritesTable.WSTYPE + " =?");
            selectionArgs.add(filterType.name());
        }

        //Add sorting type to WHERE params
        String sortOrderString;
        if (sortOrder != null && sortOrder.getValue().equals(SortOrder.CREATION_DATE.getValue())) {
            sortOrderString = FavoritesTable.CREATION_TIME + " ASC";
        } else {
            sortOrderString = FavoritesTable.TITLE + " COLLATE NOCASE ASC";
        }

        //Add search query to WHERE params
        boolean inSearchMode = searchQuery != null;
        if (inSearchMode) {
            selection.append(" AND ")
                    .append(FavoritesTable.TITLE + " LIKE ?");
            selectionArgs.add("%" + searchQuery + "%");
        }

        StringBuilder sortOrderBuilder = new StringBuilder("");
        sortOrderBuilder.append("CASE WHEN ")
                .append(FavoritesTable.WSTYPE)
                .append(" LIKE ")
                .append("'%" + ResourceType.folder + "%'")
                .append(" THEN 1 ELSE 2 END")
                .append(", ")
                .append(FavoritesTable.WSTYPE)
                .append(" COLLATE NOCASE")
                .append(", ")
                .append(sortOrderString);

        return new CursorLoader(getActivity(), JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                FavoritesTable.ALL_COLUMNS, selection.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]), sortOrderBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0) {
            setEmptyText(0);
        } else {
            setEmptyText(searchQuery == null ? R.string.f_empty_list_msg : R.string.r_search_nothing_to_display);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    //---------------------------------------------------------------------
    // Implements FavoritesAdapter.FavoritesInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onDelete(String itemTitle, final Uri itemToDelete) {
        int currentPosition = mAdapter.getCurrentPosition();
        AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(new AlertDialogFragment.PositiveClickListener() {
                    @Override
                    public void onClick(DialogFragment fragment) {
                        getActivity().getContentResolver().delete(itemToDelete, null, null);
                        mAdapter.finishActionMode();
                    }
                })
                .setTargetFragment(this, currentPosition)
                .setTitle(R.string.sdr_dfd_title)
                .setMessage(getActivity().getString(R.string.sdr_drd_msg, itemTitle))
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(android.R.string.cancel)
                .show();
    }

    @Override
    public void onInfo(String title, String description) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SimpleDialogFragment.createBuilder(getActivity(), fm)
                .setTitle(title)
                .setMessage(description)
                .setNegativeButtonText(android.R.string.ok)
                .show();
    }

}
