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
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.FavoritesAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.IResourceView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceViewHelper;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.db.database.table.FavoritesTable;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import static com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment.OnRenamedAction;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SavedItemsFragment extends RoboFragment
        implements ISimpleDialogListener, FileAdapter.FileInteractionListener, LoaderManager.LoaderCallbacks<Cursor> {

    @FragmentArg
    ViewType viewType;

    @Inject
    JsRestClient jsRestClient;

    @InjectView(android.R.id.list)
    AbsListView listView;
    @InjectView(android.R.id.empty)
    TextView emptyText;

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

        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
        super.onSaveInstanceState(outState);
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(int position) {
        mAdapter.finishActionMode();
        openReportFile(getFileByPosition(position));
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
    // Helper methods
    //---------------------------------------------------------------------

    private File getFileByPosition(int position) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        return new File(cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH)));
    }

    private void openReportFile(File reportFile) {
        Locale current = getResources().getConfiguration().locale;
        File reportOutputFile = new File(reportFile, reportFile.getName());
        String fileName = reportOutputFile.getName();
        String baseName = FileUtils.getBaseName(fileName);
        String extension = FileUtils.getExtension(fileName).toLowerCase(current);
        Uri reportOutputPath = Uri.fromFile(reportOutputFile);

        if ("HTML".equalsIgnoreCase(extension)) {
            // run the html report viewer
            SavedReportHtmlViewerActivity_.intent(this)
                    .reportFile(reportFile)
                    .resourceLabel(baseName)
                    .resourceUri(reportOutputPath.toString())
                    .start();
        } else {
            // run external viewer according to the file format
            String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection =
                SavedItemsTable.SERVER_PROFILE_ID + " =?  AND " +
                        SavedItemsTable.USERNAME + " =?  AND ";

        JsServerProfile jsServerProfile = jsRestClient.getServerProfile();
        boolean noOrganization = jsServerProfile.getOrganization() == null;
        if (noOrganization) {
            selection += SavedItemsTable.ORGANIZATION + " IS NULL";
        } else {
            selection += SavedItemsTable.ORGANIZATION + " =?";
        }
        String[] selectionArgs;
        if (noOrganization) {
            selectionArgs = new String[]{
                    String.valueOf(jsServerProfile.getId()),
                    jsServerProfile.getUsername()
            };
        } else {
            selectionArgs = new String[]{
                    String.valueOf(jsServerProfile.getId()),
                    jsServerProfile.getUsername(),
                    jsServerProfile.getOrganization()
            };
        }

        String sortOrder = SavedItemsTable.CREATION_TIME + " DESC";

        return new CursorLoader(getActivity(), JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                SavedItemsTable.ALL_COLUMNS, selection, selectionArgs, sortOrder);
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

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    //---------------------------------------------------------------------
    // Implements FileAdapter.FileInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onRename(File file, String name) {
        RenameDialogFragment.show(getFragmentManager(), file,
                name ,jsRestClient.getServerProfile().getId(),
                new OnRenamedAction() {
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
                        FileUtils.getBaseName(file.getName())))
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
        File selectedFile = getFileByPosition(position);
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
