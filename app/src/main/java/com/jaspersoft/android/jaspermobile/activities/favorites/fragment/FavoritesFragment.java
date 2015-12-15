/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.FavoritesAdapter;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SortDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ResourceOpener;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.filtering.FavoritesResourceFilter;
import com.jaspersoft.android.jaspermobile.util.filtering.Filter;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOptions;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;
import com.jaspersoft.android.jaspermobile.widget.FilterTitleView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
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
@EFragment
@OptionsMenu(R.menu.sort_menu)
public class FavoritesFragment extends RoboFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        FavoritesAdapter.FavoritesInteractionListener,
        DeleteDialogFragment.DeleteDialogClickListener,
        SortDialogFragment.SortDialogClickListener {

    public static final String TAG = FavoritesFragment.class.getSimpleName();
    private final int FAVORITES_LOADER_ID = 20;

    @Bean
    protected FavoritesResourceFilter favoritesResourceFilter;
    @Bean
    protected SortOptions sortOptions;

    @FragmentArg
    protected ViewType viewType;

    @InjectView(android.R.id.list)
    AbsListView listView;
    @InjectView(android.R.id.empty)
    TextView emptyText;
    @OptionsMenuItem(R.id.sort)
    MenuItem sortAction;

    @Inject
    protected Analytics analytics;

    @Bean
    ResourceOpener resourceOpener;

    @FragmentArg
    @InstanceState
    String searchQuery;
    private FavoritesAdapter mAdapter;


    @OptionsItem(R.id.sort)
    final void startSorting() {
        SortDialogFragment.createBuilder(getFragmentManager())
                .setInitialSortOption(sortOptions.getOrder())
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            sortOptions.putOrder(SortOrder.LABEL);
        }

        analytics.setScreenName(Analytics.ScreenName.FAVORITES.getValue());
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                (viewType == ViewType.LIST) ? R.layout.common_list_layout : R.layout.common_grid_layout,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(0);

        mAdapter = new FavoritesAdapter(getActivity(), savedInstanceState, viewType);
        mAdapter.setAdapterView(listView);
        mAdapter.setFavoritesInteractionListener(this);
        listView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        sortAction.setVisible(searchQuery == null);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(searchQuery == null ? getString(R.string.f_title) : getString(R.string.search_result_format, searchQuery));
        }

        List<Analytics.Dimension> viewDimension = new ArrayList<>();
        viewDimension.add(new Analytics.Dimension(Analytics.Dimension.FILTER_TYPE_HIT_KEY, favoritesResourceFilter.getCurrent().getName()));
        viewDimension.add(new Analytics.Dimension(Analytics.Dimension.RESOURCE_VIEW_HIT_KEY, viewType.name()));
        analytics.sendScreenView(Analytics.ScreenName.FAVORITES.getValue(),viewDimension);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getSupportLoaderManager().destroyLoader(FAVORITES_LOADER_ID);
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

        resourceOpener.openResource(this, FavoritesControllerFragment.PREF_TAG, resource);
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
        if (sortOptions.getOrder() != null && sortOptions.getOrder().getValue().equals(SortOrder.CREATION_DATE.getValue())) {
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
        mAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0) {
            setEmptyText(0);
        } else {
            setEmptyText(searchQuery == null ? R.string.f_empty_list_msg : R.string.resources_not_found);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    //---------------------------------------------------------------------
    // Implements FavoritesAdapter.FavoritesInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onDelete(String itemTitle, Uri recordUri) {
        DeleteDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setRecordUri(recordUri)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.sdr_dfd_title)
                .setMessage(getActivity().getString(R.string.sdr_dfd_msg, itemTitle))
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(R.string.cancel)
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onInfo(String itemTitle, String itemDescription) {
        SimpleDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setTitle(itemTitle)
                .setMessage(itemDescription)
                .setPositiveButtonText(getString(R.string.ok))
                .show();
    }

    //---------------------------------------------------------------------
    // Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onDeleteConfirmed(Uri recordUri, File itemFile) {
        getActivity().getContentResolver().delete(recordUri, null, null);
        mAdapter.finishActionMode();
    }

    @Override
    public void onDeleteCanceled() {
    }

    public void showFavoritesByFilter() {
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    //---------------------------------------------------------------------
    // SortDialogFragment.SortDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onOptionSelected(SortOrder sortOrder) {
        analytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.SORTED.getValue(), sortOrder.name());

        sortOptions.putOrder(sortOrder);
        getActivity().getSupportLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, this);
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    private class FilterChangeListener implements FilterTitleView.FilterListener {
        @Override
        public void onFilter(Filter filter) {
            analytics.sendEvent(Analytics.EventCategory.CATALOG.getValue(), Analytics.EventAction.FILTERED.getValue(), filter.getName());

            favoritesResourceFilter.persist(filter);
            showFavoritesByFilter();
        }
    }
}
