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

package com.jaspersoft.android.jaspermobile.activities.storage.fragment;

import android.accounts.Account;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.filtering.StorageResourceFilter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceAdapter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceConverter;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.SelectionModeHelper;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;
import com.jaspersoft.android.jaspermobile.widget.JasperRecyclerView;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.fragment_resource)
public class SavedItemsFragment extends RoboFragment
        implements DeleteDialogFragment.DeleteDialogClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, RenameDialogFragment.RenameDialogClickListener {

    public static final String TAG = SavedItemsFragment.class.getSimpleName();
    private final int SAVED_ITEMS_LOADER_ID = 10;

    @FragmentArg
    protected ViewType viewType;

    @Inject
    protected JsRestClient jsRestClient;

    @InjectView(android.R.id.list)
    protected JasperRecyclerView listView;
    @InjectView(android.R.id.empty)
    protected TextView emptyText;

    @FragmentArg
    @InstanceState
    protected String searchQuery;

    @FragmentArg
    @InstanceState
    protected SortOrder sortOrder;

    @Bean
    protected StorageResourceFilter storageResourceFilter;
    @Bean
    protected SavedItemHelper savedItemHelper;

    private SelectionModeHelper mSelectionModeHelper;
    private JasperResourceAdapter mAdapter;
    private JasperResourceConverter jasperResourceConverter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jasperResourceConverter = new JasperResourceConverter(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyText(0);
        setDataAdapter(savedInstanceState);

        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);
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

    public void showSavedItemsByFilter() {
        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);
    }

    public void showSavedItemsBySortOrder(SortOrder selectedSortOrder) {
        sortOrder = selectedSortOrder;
        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void openReportFile(File reportOutputFile, String recordUri) {
        Locale current = getResources().getConfiguration().locale;
        String fileName = reportOutputFile.getName();
        String baseName = FileUtils.getBaseName(fileName);
        String extension = FileUtils.getExtension(fileName).toLowerCase(current);

        if ("HTML".equalsIgnoreCase(extension)) {
            // run the html report viewer
            SavedReportHtmlViewerActivity_.intent(this)
                    .reportFile(reportOutputFile)
                    .resourceLabel(baseName)
                    .recordUri(recordUri)
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

    private void setDataAdapter(Bundle savedInstanceState) {
        Cursor cursor = null;
        mAdapter = new JasperResourceAdapter(jasperResourceConverter.convertToJasperResource(cursor, null, null), viewType);
        mAdapter.setOnItemInteractionListener(new JasperResourceAdapter.OnResourceInteractionListener() {
            @Override
            public void onResourceItemClicked(String id) {
                File file = jasperResourceConverter.convertToFile(id, getActivity());

                if (mSelectionModeHelper != null) {
                    mSelectionModeHelper.finishSelectionMode();
                }
                openReportFile(file, id);
            }
        });

        listView.setViewType(viewType);
        listView.setAdapter(mAdapter);
        mSelectionModeHelper = new SavedItemsSelectionModeHelper(mAdapter);
        mSelectionModeHelper.restoreState(savedInstanceState);
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
        selection
                .append("(")
                .append(SavedItemsTable.ACCOUNT_NAME + " =?")
                .append("  OR ");

        selectionArgs.add(JasperSettings.RESERVED_ACCOUNT_NAME);

        //Add server profile id to WHERE params
        selection.append(SavedItemsTable.ACCOUNT_NAME + " =?");
        selectionArgs.add(account.name);

        // Close select brackets
        selection.append(")");

        //Add filtration to WHERE params
        selection.append(" AND (");

        Iterator<String> iterator = storageResourceFilter.getCurrent().getValues().iterator();
        while (iterator.hasNext()) {
            selection.append(SavedItemsTable.FILE_FORMAT + " =?");
            selectionArgs.add(iterator.next());
            if (iterator.hasNext()) {
                selection.append(" OR ");
            }
        }

        selection.append(")");

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
        mAdapter.clear();
        mAdapter.addAll(jasperResourceConverter.convertToJasperResource(cursor));
        mAdapter.notifyDataSetChanged();

        if (cursor.getCount() > 0) {
            setEmptyText(0);
        } else {
            setEmptyText(searchQuery == null ? R.string.sdr_ab_list_msg : R.string.resources_not_found);
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
    public void onDeleteConfirmed(List<String> itemsToDelete, List<File> filesToDelete) {
        for (int i = 0; i < filesToDelete.size(); i++) {
            long id = Long.valueOf(Uri.parse(itemsToDelete.get(i)).getLastPathSegment());
            savedItemHelper.deleteSavedItem(filesToDelete.get(i), id);
        }

        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);

        mSelectionModeHelper.finishSelectionMode();
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

        getActivity().getSupportLoaderManager().restartLoader(SAVED_ITEMS_LOADER_ID, null, this);

        mSelectionModeHelper.finishSelectionMode();
    }

    //---------------------------------------------------------------------
    // Library selection mode helper
    //---------------------------------------------------------------------

    private class SavedItemsSelectionModeHelper extends SelectionModeHelper<String> {

        public SavedItemsSelectionModeHelper(JasperResourceAdapter resourceAdapter) {
            super(((ActionBarActivity) getActivity()), resourceAdapter);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.am_saved_items_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            menu.findItem(R.id.showAction).setVisible(getSelectedItemCount() == 1);
            menu.findItem(R.id.renameItem).setVisible(getSelectedItemCount() == 1);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            ArrayList<String> selectedItemIds = getSelectedItemsKey();
            if (selectedItemIds.size() == 0) return false;

            List<File> selectedFiles = new ArrayList<>();
            for (String selectedItemId : selectedItemIds) {
                selectedFiles.add(jasperResourceConverter.convertToFile(selectedItemId, getActivity()));
            }

            switch (menuItem.getItemId()) {
                case R.id.showAction:
                    Cursor cursor = getActivity().getContentResolver().query(Uri.parse(selectedItemIds.get(0)), null, null, null, null);
                    cursor.moveToFirst();

                    String title = cursor.getString(cursor.getColumnIndex(SavedItemsTable.NAME));
                    long creationTime = cursor.getLong(cursor.getColumnIndex(SavedItemsTable.CREATION_TIME));

                    String description = String.format("%s \n %s", getFormattedDateModified(creationTime),
                            getHumanReadableFileSize(selectedFiles.get(0)));
                    onInfo(title, description);
                    return true;
                case R.id.deleteItem:
                    onDelete(selectedFiles, selectedItemIds);
                    invalidateSelectionMode();
                    return true;
                case R.id.renameItem:
                    cursor = getActivity().getContentResolver().query(Uri.parse(selectedItemIds.get(0)), null, null, null, null);
                    cursor.moveToFirst();

                    String fileExtension = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));
                    onRename(selectedFiles.get(0).getParentFile(), Uri.parse(selectedItemIds.get(0)), fileExtension);
                    return true;
                default:
                    return false;
            }
        }

        public void onRename(File itemFile, Uri recordUri, String fileExtension) {
            RenameDialogFragment.createBuilder(getFragmentManager())
                    .setSelectedFile(itemFile)
                    .setExtension(fileExtension)
                    .setRecordUri(recordUri)
                    .setTargetFragment(SavedItemsFragment.this)
                    .show();
        }

        public void onDelete(List<File> itemsFile, ArrayList<String> recordsUri) {
            String deleteMessage;
            if (itemsFile.size() > 1) {
                deleteMessage = getActivity().getString(R.string.sdr_drd_msg_multi, itemsFile.size());
            } else {
                deleteMessage = getActivity().getString(R.string.sdr_drd_msg, itemsFile.get(0).getName());
            }
            DeleteDialogFragment.createBuilder(getActivity(), getFragmentManager())
                    .setFiles(itemsFile)
                    .setRecordsUri(recordsUri)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.sdr_drd_title)
                    .setMessage(deleteMessage)
                    .setPositiveButtonText(R.string.spm_delete_btn)
                    .setNegativeButtonText(R.string.cancel)
                    .setTargetFragment(SavedItemsFragment.this)
                    .show();
        }

        public void onInfo(String title, String description) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            SimpleDialogFragment.createBuilder(getActivity(), fm)
                    .setTitle(title)
                    .setMessage(description)
                    .setPositiveButtonText(R.string.ok)
                    .show();
        }

        private String getFormattedDateModified(long creationTime) {
            return DateUtils.formatDateTime(getActivity(), creationTime,
                    DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_TIME |
                            DateUtils.FORMAT_SHOW_YEAR |
                            DateUtils.FORMAT_NUMERIC_DATE |
                            DateUtils.FORMAT_24HOUR
            );
        }

        private String getHumanReadableFileSize(File file) {
            long byteSize = FileUtils.calculateFileSize(file.getParentFile());
            return FileUtils.getHumanReadableByteCount(byteSize);
        }
    }
}
