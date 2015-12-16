package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.jaspermobile.widget.InfoView;

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
public class SavedItemInfoFragment extends SimpleInfoFragment implements DeleteDialogFragment.DeleteDialogClickListener, RenameDialogFragment.RenameDialogClickListener {

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
    protected void showSavedItemInfo(){
        infoView.fillWithBaseData(jasperResource.getResourceType().name(), jasperResource.getLabel(),
                jasperResource.getDescription(), mFileUri,
                mCreationTime, null);
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
                .setFile(new File(mFileUri))
                .setRecordsUri(jasperResource.getId())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.sdr_drd_title)
                .setMessage(deleteMessage)
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(R.string.cancel)
                .setTargetFragment(this)
                .show();
    }

    public void fetchSavedItemMetadata() {
        Cursor cursor = getActivity().getContentResolver().query(Uri.parse(jasperResource.getId()), null, null, null, null);
        cursor.moveToFirst();

        mFileUri = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH));
        mFileExtension = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));

        long creationTime = cursor.getLong(cursor.getColumnIndex(SavedItemsTable.CREATION_TIME));
        Date creationDate = new Date(creationTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        mCreationTime = simpleDateFormat.format(creationDate);
    }

    //---------------------------------------------------------------------
    // Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onDeleteConfirmed(String itemsToDelete, File filesToDelete) {
        long id = Long.valueOf(Uri.parse(itemsToDelete).getLastPathSegment());
        savedItemHelper.deleteSavedItem(filesToDelete, id);
        getActivity().finish();
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

        mFileUri = newFilePath;

        updateHeaderViewLabel(newFileName);
        showSavedItemInfo();
    }
}
