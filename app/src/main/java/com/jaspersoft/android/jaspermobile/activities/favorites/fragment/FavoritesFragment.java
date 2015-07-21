/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.filtering.FavoritesResourceFilter;
import com.jaspersoft.android.jaspermobile.util.filtering.Filter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceAdapter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceConverter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.SelectionModeHelper;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOptions;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;
import com.jaspersoft.android.jaspermobile.widget.FilterTitleView;
import com.jaspersoft.android.jaspermobile.widget.JasperRecyclerView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.fragment_resource)
public class FavoritesFragment extends RoboFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteDialogFragment.DeleteDialogClickListener {

    public static final String TAG = FavoritesFragment.class.getSimpleName();
    private final int FAVORITES_LOADER_ID = 20;

    @Bean
    protected FavoritesResourceFilter favoritesResourceFilter;
    @Bean
    protected SortOptions sortOptions;

    @FragmentArg
    protected ViewType viewType;
    @FragmentArg
    @InstanceState
    protected SortOrder sortOrder;

    @InjectView(android.R.id.list)
    JasperRecyclerView listView;
    @InjectView(android.R.id.empty)
    TextView emptyText;

    @Inject
    JsRestClient jsRestClient;

    @Bean
    ResourceOpener resourceOpener;

    @FragmentArg
    @InstanceState
    String searchQuery;

    private SelectionModeHelper mSelectionModeHelper;
    private JasperResourceAdapter mAdapter;
    JasperResourceConverter jasperResourceConverter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        jasperResourceConverter = new JasperResourceConverter(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (searchQuery == null) {
            FilterTitleView filterTitleView = new FilterTitleView(getActivity());
            filterTitleView.init(favoritesResourceFilter);
            filterTitleView.setFilterSelectedListener(new FilterChangeListener());
            ((RoboToolbarActivity) getActivity()).setDisplayCustomToolbarEnable(true);
            ((RoboToolbarActivity) getActivity()).setCustomToolbarView(filterTitleView);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(0);

        setDataAdapter(savedInstanceState);

        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(searchQuery == null ? getString(R.string.f_title) : getString(R.string.search_result_format, searchQuery));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mSelectionModeHelper.saveState(outState);
        super.onSaveInstanceState(outState);
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

    public void showFavoritesByFilter() {
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    public void showFavoritesBySortOrder(SortOrder selectedSortOrder) {
        sortOrder = selectedSortOrder;
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    private void onViewSingleClick(ResourceLookup resource) {
        if (mSelectionModeHelper != null) {
            mSelectionModeHelper.finishSelectionMode();
        }
        resourceOpener.openResource(this, FavoritesControllerFragment.PREF_TAG, resource);
    }

    private void setDataAdapter(Bundle savedInstanceState) {
        Cursor cursor = null;
        mAdapter = new JasperResourceAdapter(jasperResourceConverter.convertToJasperResource(cursor, null, null), viewType);
        mAdapter.setOnItemInteractionListener(new JasperResourceAdapter.OnResourceInteractionListener() {
            @Override
            public void onResourceItemClicked(String id) {
                ResourceLookup resource = jasperResourceConverter.convertToResourceLookup(id, getActivity());
                onViewSingleClick(resource);
            }
        });

        listView.setViewType(viewType);
        listView.setAdapter(mAdapter);
        mSelectionModeHelper = new LibrarySelectionModeHelper(mAdapter);
        mSelectionModeHelper.restoreState(savedInstanceState);
    }

    //---------------------------------------------------------------------
    // Implements LoaderManager.LoaderCallbacks<Cursor>
    //---------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        StringBuilder selection = new StringBuilder("");
        ArrayList<String> selectionArgs = new ArrayList<String>();

        //Add server profile id and username to WHERE params
        selection.append(FavoritesTable.ACCOUNT_NAME + " =?");
        selectionArgs.add(JasperAccountManager.get(getActivity()).getActiveAccount().name);

        //Add filtration to WHERE params
        selection.append(" AND (");

        Iterator<String> iterator = favoritesResourceFilter.getCurrent().getValues().iterator();
        while (iterator.hasNext()) {
            selection.append(FavoritesTable.WSTYPE + " =?");
            selectionArgs.add(iterator.next());
            if (iterator.hasNext()) {
                selection.append(" OR ");
            }
        }

        selection.append(")");

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
                .append(sortOrderString);

        return new CursorLoader(getActivity(), JasperMobileDbProvider.FAVORITES_CONTENT_URI,
                FavoritesTable.ALL_COLUMNS, selection.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]), sortOrderBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.clear();
        mAdapter.addAll(jasperResourceConverter.convertToJasperResource(cursor, FavoritesTable._ID, JasperMobileDbProvider.FAVORITES_CONTENT_URI));
        mAdapter.notifyDataSetChanged();

        if (cursor.getCount() > 0) {
            setEmptyText(0);
        } else {
            setEmptyText(searchQuery == null ? R.string.f_empty_list_msg : R.string.r_search_nothing_to_display);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    //---------------------------------------------------------------------
    // Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onDeleteConfirmed(List<String> recordsUri, List<File> itemsFile) {
        for (String recordUri : recordsUri) {
            getActivity().getContentResolver().delete(Uri.parse(recordUri), null, null);
        }
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
        mSelectionModeHelper.finishSelectionMode();
    }

    @Override
    public void onDeleteCanceled() {
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    private class FilterChangeListener implements FilterTitleView.FilterListener {
        @Override
        public void onFilter(Filter filter) {
            favoritesResourceFilter.persist(filter);
            showFavoritesByFilter();
        }
    }

    //---------------------------------------------------------------------
    // Library selection mode helper
    //---------------------------------------------------------------------

    private class LibrarySelectionModeHelper extends SelectionModeHelper<String> {

        public LibrarySelectionModeHelper(JasperResourceAdapter resourceAdapter) {
            super(((ActionBarActivity) getActivity()), resourceAdapter);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.am_favorites_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            menu.findItem(R.id.showAction).setVisible(getSelectedItemCount() == 1);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            ArrayList<String> selectedItemIds = getSelectedItemsKey();
            if (selectedItemIds.size() == 0) return false;

            ResourceLookup selectedItem;
            switch (menuItem.getItemId()) {
                case R.id.showAction:
                    selectedItem = jasperResourceConverter.convertToResourceLookup(selectedItemIds.get(0), getActivity());
                    String resourceTitle = selectedItem.getLabel();
                    String resourceDescription = selectedItem.getDescription();

                    showInfo(resourceTitle, resourceDescription);
                    return true;
                case R.id.removeFromFavorites:
                    String deleteMessage;
                    if (selectedItemIds.size() > 1) {
                        deleteMessage = getActivity().getString(R.string.sdr_dfd_msg_multi, selectedItemIds.size());
                    } else {
                        selectedItem = jasperResourceConverter.convertToResourceLookup(selectedItemIds.get(0), getActivity());
                        deleteMessage = getActivity().getString(R.string.sdr_dfd_msg, selectedItem.getLabel());
                    }
                    DeleteDialogFragment.createBuilder(getActivity(), getFragmentManager())
                            .setRecordsUri(selectedItemIds)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.sdr_dfd_title)
                            .setMessage(deleteMessage)
                            .setPositiveButtonText(R.string.spm_delete_btn)
                            .setNegativeButtonText(R.string.cancel)
                            .setTargetFragment(FavoritesFragment.this)
                            .show();
                    invalidateSelectionMode();
                    return true;
                default:
                    return false;
            }
        }

        private void showInfo(String resourceTitle, String resourceDescription) {
            SimpleDialogFragment.createBuilder(getActivity(), getFragmentManager())
                    .setTitle(resourceTitle)
                    .setMessage(resourceDescription)
                    .setPositiveButtonText(getString(R.string.ok))
                    .show();
        }
    }
}
