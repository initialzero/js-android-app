package com.jaspersoft.android.jaspermobile.activities.storage.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.RenameDialogFragment;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;

import java.io.File;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
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
        implements ISimpleDialogListener, FileAdapter.FileInteractionListener {

    @FragmentArg
    ViewType viewType;

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

        emptyText.setVisibility(View.GONE);
        mAdapter = FileAdapter.builder(getActivity(), savedInstanceState)
                .setViewType(viewType).create();
        mAdapter.setFileInteractionListener(this);
        mAdapter.setAdapterView(listView);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReportsListView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
        super.onSaveInstanceState(outState);
    }

    @ItemClick(android.R.id.list)
    public void onItemClick(File file) {
        mAdapter.finishActionMode();
        openReportFile(file);
    }

    private void openReportFile(File reportFile) {
        File reportOutputFile = new File(reportFile, reportFile.getName());
        String fileName = reportOutputFile.getName();
        String baseName = FileUtils.getBaseName(fileName);
        String extension = FileUtils.getExtension(fileName).toLowerCase();
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

    @UiThread
    protected void setEmptyText(int resId) {
        if (resId == 0) {
            emptyText.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(resId);
        }
    }

    private void loadReportsListView() {
        File[] files = getSavedReportsDir().listFiles();

        if (files != null && files.length > 0) {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.addAll(files);
            mAdapter.sortByLstModified();
            mAdapter.setNotifyOnChange(true);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.clear();
            setEmptyText(R.string.r_browser_nothing_to_display);
        }
    }

    private File getSavedReportsDir() {
        File appFilesDir = getActivity().getExternalFilesDir(null);
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);

        if (!savedReportsDir.exists() && !savedReportsDir.mkdirs()){
            Ln.e("Unable to create %s", savedReportsDir);
        }

        return savedReportsDir;
    }

    //---------------------------------------------------------------------
    // Implements FileAdapter.FileInteractionListener
    //---------------------------------------------------------------------

    @Override
    public void onRename(File file) {
        RenameDialogFragment.show(getFragmentManager(), file,
                new OnRenamedAction() {
                    @Override
                    public void onRenamed() {
                        mAdapter.finishActionMode();
                        loadReportsListView();
                    }
                });
    }

    @Override
    public void onDelete(int currentPosition, File file) {
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

    //---------------------------------------------------------------------
    // Implements ISimpleDialogListener
    //---------------------------------------------------------------------

    @Override
    public void onPositiveButtonClicked(int position) {
        File selectedFile = mAdapter.getItem(position);
        if (selectedFile.isDirectory()) {
            FileUtils.deleteFilesInDirectory(selectedFile);
        }

        if (selectedFile.delete()) {
            loadReportsListView();
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
