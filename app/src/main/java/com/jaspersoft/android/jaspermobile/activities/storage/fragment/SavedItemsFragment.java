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

package com.jaspersoft.android.jaspermobile.activities.storage.fragment;

import android.accounts.Account;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SavedItemsFragment extends RoboFragment
        implements FileAdapter.FileInteractionListener, DeleteDialogFragment.DeleteDialogClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, RenameDialogFragment.RenameDialogClickListener {

    private final int SAVED_ITEMS_LOADER_ID = 10;

    @FragmentArg
    protected ViewType viewType;

    @Inject
    protected JsRestClient jsRestClient;

    @InjectView(android.R.id.list)
    protected AbsListView listView;
    @InjectView(android.R.id.empty)
    protected TextView emptyText;

    @FragmentArg
    @InstanceState
    protected FileAdapter.FileType filterType;

    @FragmentArg
    @InstanceState
    protected String searchQuery;

    @FragmentArg
    @InstanceState
    protected SortOrder sortOrder;

    @Bean
    protected SavedItemHelper savedItemHelper;

    private FileAdapter mAdapter;

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
        mAdapter = new FileAdapter(getActivity(), savedInstanceState, layout, viewType);
        mAdapter.setAdapterView(listView);
        mAdapter.setFileInteractionListener(this);
        listView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
        super.onSaveInstanceState(outState);
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(int position) {
        mAdapter.finishActionMode();
        openReportFile(position);
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

    public void showSavedItemsByFilter(FileAdapter.FileType selectedFilter) {
        filterType = selectedFilter;
        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);
    }

    public void showSavedItemsBySortOrder(SortOrder selectedSortOrder) {
        sortOrder = selectedSortOrder;
        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private File getFileByPosition(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        return new File(cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH)));
    }

    private long getIndexByPosition(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        return cursor.getLong(cursor.getColumnIndex(SavedItemsTable._ID));
    }

    private void openReportFile(int position) {
        File reportOutputFile = getFileByPosition(position);

        Locale current = getResources().getConfiguration().locale;
        String fileName = reportOutputFile.getName();
        String baseName = FileUtils.getBaseName(fileName);
        String extension = FileUtils.getExtension(fileName).toLowerCase(current);

        if ("HTML".equalsIgnoreCase(extension)) {
            // run the html report viewer
            SavedReportHtmlViewerActivity_.intent(this)
                    .reportFile(reportOutputFile)
                    .resourceLabel(baseName)
                    .reportId(getIndexByPosition(position))
                    .start();
        } else {
            // run external viewer according to the file format
            String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            Uri reportOutputPath = Uri.fromFile(reportOutputFile);
            Intent externalViewer = new Intent(Intent.ACTION_VIEW);
            externalViewer.setDataAndType(reportOutputPath, contentType);
            externalViewer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(externalViewer);
            } catch (ActivityNotFoundException e) {
                // show notification if no app available to open selected format
                Toast.makeText(getActivity(),
                        getString(R.string.sdr_t_no_app_available, extension), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //---------------------------------------------------------------------
    // Implements LoaderManager.LoaderCallbacks<Cursor>
    //---------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int code, Bundle bundle) {
        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        StringBuilder selection = new StringBuilder("");
        ArrayList<String> selectionArgs = new ArrayList<String>();

        //Add general items (server id = -1)
        selection.append(SavedItemsTable.ACCOUNT_NAME + " =?")
                .append("  OR ")
                .append("(");

        selectionArgs.add(JasperSettings.RESERVED_ACCOUNT_NAME);

        //Add server profile id to WHERE params
        selection.append(SavedItemsTable.ACCOUNT_NAME + " =?");
        selectionArgs.add(account.name);

        // Close select brackets
        selection.append(")");

        //Add filtration to WHERE params
        boolean withFiltering = filterType != null;
        if (withFiltering) {
            selection.append(" AND ")
                    .append(SavedItemsTable.FILE_FORMAT + " =?");
            selectionArgs.add(filterType.name());
        }

        //Add sorting type to WHERE params
        String sortOrderString;
        if (sortOrder != null && sortOrder.getValue().equals(SortOrder.CREATION_DATE.getValue())) {
            sortOrderString = SavedItemsTable.CREATION_TIME + " DESC";
        } else {
            sortOrderString = SavedItemsTable.NAME + " COLLATE NOCASE ASC";
        }

        //Add search query to WHERE params
        boolean inSearchMode = searchQuery != null;
        if (inSearchMode) {
            selection.append(" AND ")
                    .append(SavedItemsTable.NAME + " LIKE ?");
            selectionArgs.add("%" + searchQuery + "%");
        }

        return new CursorLoader(getActivity(), JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                SavedItemsTable.ALL_COLUMNS, selection.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]), sortOrderString);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0) {
            setEmptyText(0);
        } else {
            setEmptyText(searchQuery == null ? R.string.sdr_ab_list_msg : R.string.r_search_nothing_to_display);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    //---------------------------------------------------------------------
    // Implements FileAdapter.FileInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onRename(File itemFile, Uri recordUri, String fileExtension) {
        RenameDialogFragment.createBuilder(getFragmentManager())
                .setSelectedFile(itemFile)
                .setExtension(fileExtension)
                .setRecordUri(recordUri)
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onDelete(File itemFile, Uri recordUri) {
        DeleteDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setFile(itemFile)
                .setRecordUri(recordUri)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.sdr_drd_title)
                .setMessage(getActivity().getString(R.string.sdr_drd_msg,
                        itemFile.getName()))
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(android.R.string.cancel)
                .setTargetFragment(this)
                .show();
    }

    @Override
    public void onInfo(String title, String description) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SimpleDialogFragment.createBuilder(getActivity(), fm)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButtonText(android.R.string.ok)
                .setTargetFragment(this)
                .show();
    }

    //---------------------------------------------------------------------
    // Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onDeleteConfirmed(Uri itemToDelete, File fileToDelete) {
        long id = Long.valueOf(itemToDelete.getLastPathSegment());
        savedItemHelper.deleteSavedItem(fileToDelete, id);
        mAdapter.finishActionMode();
    }

    @Override
    public void onDeleteCanceled() {
    }

    //---------------------------------------------------------------------
    // Implements RenameDialogFragment.RenameDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onRenamed(String newFileName, String newFilePath, Uri recordUri) {
        SavedItems savedItemsEntry = new SavedItems();
        savedItemsEntry.setName(newFileName);
        savedItemsEntry.setFilePath(newFilePath);

        getActivity().getContentResolver().update(recordUri, savedItemsEntry.getContentValues(), null, null);

        mAdapter.finishActionMode();
    }
}
