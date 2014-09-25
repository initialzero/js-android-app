package com.jaspersoft.android.jaspermobile.activities.storage.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity_;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
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

import static com.jaspersoft.android.jaspermobile.activities.storage.fragment.RenameDialogFragment.OnRenamedAction;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SavedItemsFragment extends RoboFragment implements ISimpleDialogListener {

    // Context menu IDs
    private static final int ID_CM_OPEN = 10;
    private static final int ID_CM_RENAME = 11;
    private static final int ID_CM_DELETE = 12;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(listView);
        emptyText.setVisibility(View.GONE);
        mAdapter = FileAdapter.builder(getActivity()).setViewType(viewType).create();
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReportsListView();
    }

    @ItemClick(android.R.id.list)
    final void itemClicked(File reportFile) {
        openReportFile(reportFile);
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
    // Implements Context Menu
    //---------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        // Determine on which item in the ListView the user long-clicked
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        File selectedFile = mAdapter.getItem(info.position);

        String baseName = FileUtils.getBaseName(selectedFile.getName());
        menu.setHeaderTitle(baseName);

        // Add all the menu options
        menu.add(Menu.NONE, ID_CM_OPEN, Menu.NONE, R.string.sdr_cm_open);
        menu.add(Menu.NONE, ID_CM_RENAME, Menu.NONE, R.string.sdr_cm_rename);
        menu.add(Menu.NONE, ID_CM_DELETE, Menu.NONE, R.string.sdr_cm_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Determine on which item in the ListView the user long-clicked and get it from Cursor
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        File reportFile = mAdapter.getItem(info.position);
        // Handle item selection
        switch (item.getItemId()) {
            case ID_CM_OPEN:
                openReportFile(reportFile);
                return true;
            case ID_CM_RENAME:
                RenameDialogFragment.show(getFragmentManager(), reportFile, new OnRenamedAction() {
                    @Override
                    public void onRenamed() {
                        loadReportsListView();
                    }
                });
                return true;
            case ID_CM_DELETE:
                AlertDialogFragment.createBuilder(getActivity(), getFragmentManager())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTargetFragment(this, info.position)
                        .setTitle(R.string.sdr_drd_title)
                        .setMessage(getActivity().getString(R.string.sdr_drd_msg,
                                FileUtils.getBaseName(reportFile.getName())))
                        .setPositiveButtonText(R.string.spm_delete_btn)
                        .setNegativeButtonText(android.R.string.cancel)
                        .show();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onContextItemSelected(item);
        }
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
    }

    @Override
    public void onNegativeButtonClicked(int i) {
    }

    @Override
    public void onNeutralButtonClicked(int i) {
    }
}
