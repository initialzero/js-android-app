/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.InfoView;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@OptionsMenu(R.menu.am_saved_items_menu)
@EFragment(R.layout.fragment_resource_info)
public class SavedItemInfoFragment extends SimpleInfoFragment
        implements DeleteDialogFragment.DeleteDialogClickListener,
        RenameDialogFragment.RenameDialogClickListener {

    @ViewById(R.id.infoDetailsView)
    protected InfoView infoView;

    @Bean
    protected SavedItemHelper savedItemHelper;

    private String mFileUri;
    private String mFileExtension;
    private String mCreationTime;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchSavedItemMetadata();
    }

    @AfterViews
    protected void showSavedItemInfo() {
        infoView.fillWithBaseData(jasperResource.getResourceType().name(), jasperResource.getLabel(),
                jasperResource.getDescription(), mFileUri,
                mCreationTime, null, null, -1);

        infoView.addInfoItem(getString(R.string.ri_file_format), mFileExtension, 1);
        infoView.addInfoItem(getString(R.string.ri_file_size), getHumanReadableFileSize(new File(mFileUri)), 4);
    }

    @OptionsItem(R.id.renameItem)
    protected void renameSavedFile() {
        RenameDialogFragment.createBuilder(getFragmentManager())
                .setSelectedFile(new File(mFileUri).getParentFile())
                .setExtension(mFileExtension)
                .setRecordUri(Uri.parse(jasperResource.getId()))
                .setTargetFragment(this)
                .show();
    }

    @OptionsItem(R.id.deleteItem)
    protected void deleteSavedFile() {
        String deleteMessage = getActivity().getString(R.string.sdr_drd_msg, jasperResource.getLabel());

        DeleteDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setResource(jasperResource)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.sdr_drd_title)
                .setMessage(deleteMessage)
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(R.string.cancel)
                .setTargetFragment(this)
                .show();
    }

    private void fetchSavedItemMetadata() {
        Cursor cursor = getActivity().getContentResolver().query(Uri.parse(jasperResource.getId()), null, null, null, null);
        cursor.moveToFirst();

        mFileUri = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH));
        mFileExtension = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));

        long creationTime = cursor.getLong(cursor.getColumnIndex(SavedItemsTable.CREATION_TIME));
        Date creationDate = new Date(creationTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        mCreationTime = simpleDateFormat.format(creationDate);
    }

    private String getHumanReadableFileSize(File file) {
        long byteSize = FileUtils.calculateFileSize(file.getParentFile());
        return FileUtils.getHumanReadableByteCount(byteSize);
    }

    //---------------------------------------------------------------------
    // Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onDeleteConfirmed(JasperResource resource) {
        savedItemHelper.deleteSavedItem(Uri.parse(resource.getId()));
        getActivity().finish();
    }

    //---------------------------------------------------------------------
    // Implements RenameDialogFragment.RenameDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onRenamed(String newFileName, Uri recordUri) {
        if (savedItemHelper.renameSavedItem(recordUri, newFileName)) {
            fetchSavedItemMetadata();
            updateHeaderViewLabel(newFileName);
            showSavedItemInfo();
        } else {
            Toast.makeText(getActivity(), R.string.sdr_t_report_renaming_error, Toast.LENGTH_SHORT).show();
        }
    }
}
