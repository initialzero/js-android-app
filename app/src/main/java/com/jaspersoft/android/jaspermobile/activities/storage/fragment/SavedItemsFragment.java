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

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment.OnRenamedAction;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SavedItemsFragment extends RoboFragment
        implements ISimpleDialogListener, FileAdapter.FileInteractionListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final int SAVED_ITEMS_LOADER_ID = 0;

    @FragmentArg
    ViewType viewType;

    @Inject
    JsRestClient jsRestClient;

    @InjectView(android.R.id.list)
    AbsListView listView;
    @InjectView(android.R.id.empty)
    TextView emptyText;

    @FragmentArg
    @InstanceState
    FileAdapter.FileType filterType;

    @FragmentArg
    @InstanceState
    String searchQuery;

    @FragmentArg
    @InstanceState
    SortOrder sortOrder;

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
        File reportFile = getFileByPosition(position);

        Locale current = getResources().getConfiguration().locale;
        String fileName = reportFile.getName();
        String baseName = FileUtils.getBaseName(fileName);
        String extension = FileUtils.getExtension(fileName).toLowerCase(current);

        if ("HTML".equalsIgnoreCase(extension)) {
            // run the html report viewer
            SavedReportHtmlViewerActivity_.intent(this)
                    .reportFile(reportFile)
                    .resourceLabel(baseName)
                    .reportId(getIndexByPosition(position))
                    .start();
        } else {
            // run external viewer according to the file format
            String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            Uri reportOutputPath = Uri.fromFile(reportFile);
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
        StringBuilder selection = new StringBuilder("");
        ArrayList<String> selectionArgs = Lists.newArrayList();

        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        boolean noOrganization = jsServerProfile.getOrganization() == null;

        //Add general items (server id = -1)
        selection.append(SavedItemsTable.SERVER_PROFILE_ID + " =?")
                .append("  OR ")
                .append("(");

        selectionArgs.add("-1");

        //Add server profile id and username to WHERE params
        selection.append(SavedItemsTable.SERVER_PROFILE_ID + " =?")
                .append("  AND ")
                .append(SavedItemsTable.USERNAME + " =?") ;

        selectionArgs.add(String.valueOf(jsServerProfile.getId()));
        selectionArgs.add(String.valueOf(jsServerProfile.getUsername()));

        //Add organization to WHERE params
        if (noOrganization) {
            selection.append("  AND ")
                    .append(SavedItemsTable.ORGANIZATION + " IS NULL");
        } else {
            selection.append("  AND ")
                    .append(SavedItemsTable.ORGANIZATION + " =?");
            selectionArgs.add(String.valueOf(jsServerProfile.getOrganization()));
        }

        // Close select brackets
        selection .append(")");

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

    }

    //---------------------------------------------------------------------
    // Implements FileAdapter.FileInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onRename(File file, String extension) {
        RenameDialogFragment.show(getFragmentManager(), file,
                extension, new OnRenamedAction() {
                    @Override
                    public void onRenamed(String newFileName, String newFilePath) {
                        long id = Lists.newArrayList(mAdapter.getCheckedItems()).get(0);
                        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                                String.valueOf(id));

                        SavedItems savedItemsEntry = new SavedItems();
                        savedItemsEntry.setName(newFileName);
                        savedItemsEntry.setFilePath(newFilePath);

                        getActivity().getContentResolver().update(uri, savedItemsEntry.getContentValues(), null, null);

                        mAdapter.finishActionMode();
                    }
                });
    }

    @Override
    public void onDelete(File file) {
        int currentPosition = mAdapter.getCurrentPosition();
        AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTargetFragment(this, currentPosition)
                .setTitle(R.string.sdr_drd_title)
                .setMessage(getActivity().getString(R.string.sdr_drd_msg,
                        file.getName()))
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
                .show();
    }

    //---------------------------------------------------------------------
    // Implements ISimpleDialogListener
    //---------------------------------------------------------------------

    @Override
    public void onPositiveButtonClicked(int position) {
        File selectedFile = getFileByPosition(position).getParentFile();
        if (selectedFile.isDirectory()) {
            FileUtils.deleteFilesInDirectory(selectedFile);
        }

        if (selectedFile.delete() || !selectedFile.exists()) {
            long id = Lists.newArrayList(mAdapter.getCheckedItems()).get(0);
            Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                    String.valueOf(id));
            getActivity().getContentResolver().delete(uri, null, null);
        } else {
            Toast.makeText(getActivity(), R.string.sdr_t_report_deletion_error, Toast.LENGTH_SHORT).show();
        }
        mAdapter.finishActionMode();
    }

    @Override
    public void onNegativeButtonClicked(int i) {
    }

    @Override
    public void onNeutralButtonClicked(int i) {
    }

}
