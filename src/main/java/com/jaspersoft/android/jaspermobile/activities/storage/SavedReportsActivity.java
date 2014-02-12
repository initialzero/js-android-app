/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.storage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.BaseHtmlViewerActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.SavedReportHtmlViewerActivity;
import com.jaspersoft.android.sdk.ui.adapters.FileArrayAdapter;
import com.jaspersoft.android.sdk.util.FileUtils;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

/**
 * @author Ivan Gadzhega
 * @since 1.8
 */

public class SavedReportsActivity extends RoboSherlockListActivity {

    // Context menu IDs
    private static final int ID_CM_OPEN = 10;
    private static final int ID_CM_RENAME = 11;
    private static final int ID_CM_DELETE = 12;
    // Dialog IDs
    private static final int ID_D_RENAME_REPORT = 20;
    private static final int ID_D_DELETE_REPORT = 21;

    @InjectView(R.id.nothingToDisplayText)
    private TextView nothingToDisplayText;

    @InjectView(android.R.id.list)
    private ListView listView;

    private File selectedFile;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repository_layout);

        // set empty view
        listView.setEmptyView(nothingToDisplayText);
        // Register a context menu to be shown for the given view
        registerForContextMenu(listView);
        //update title
        getSupportActionBar().setTitle(R.string.sdr_ab_title);

        updateReportsListView();
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        File reportFile = (File) getListView().getItemAtPosition(position);
        openReportFile(reportFile);
    }

    //---------------------------------------------------------------------
    // Options Menu
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                HomeActivity.goHome(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Context menu
    //---------------------------------------------------------------------

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        // Determine on which item in the ListView the user long-clicked and get corresponding file
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedFile = (File) getListView().getItemAtPosition(info.position);
        // set title for the menu
        String baseName = FileUtils.getBaseName(selectedFile.getName());
        menu.setHeaderTitle(baseName);
        // Add all the menu options
        menu.add(Menu.NONE, ID_CM_OPEN, Menu.NONE, R.string.sdr_cm_open);
        menu.add(Menu.NONE, ID_CM_RENAME, Menu.NONE, R.string.sdr_cm_rename);
        menu.add(Menu.NONE, ID_CM_DELETE, Menu.NONE, R.string.sdr_cm_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_CM_OPEN:
                openReportFile(selectedFile);
                return true;
            case ID_CM_RENAME:
                showDialog(ID_D_RENAME_REPORT);
                return true;
            case ID_CM_DELETE:
                showDialog(ID_D_DELETE_REPORT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Dialogs
    //---------------------------------------------------------------------

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case ID_D_RENAME_REPORT:
                return createReportRenamingDialog();
            case ID_D_DELETE_REPORT:
                return createReportDeletionDialog();
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        String fileName = FileUtils.getBaseName(selectedFile.getName());
        switch (id) {
            case ID_D_RENAME_REPORT:
                EditText reportNameEdit = (EditText) dialog.findViewById(R.id.report_name_input);
                reportNameEdit.setText(fileName);
                break;
            case ID_D_DELETE_REPORT:
                String message = getString(R.string.sdr_drd_msg, fileName);
                ((AlertDialog) dialog).setMessage(message);
                break;
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private Dialog createReportRenamingDialog() {
        final View customLayout = getLayoutInflater().inflate(R.layout.rename_report_dialog_layout, null);
        final EditText reportNameEdit = (EditText) customLayout.findViewById(R.id.report_name_input);
        final TextView reportNameError = (TextView) customLayout.findViewById(R.id.report_name_error);

        reportNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reportNameError.setVisibility(View.GONE);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // inflate custom layout
        builder.setView(customLayout);
        // define title
        builder.setTitle(R.string.sdr_rrd_title);
        // define actions
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        final AlertDialog dialog = builder.create();
        // on click listener
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newReportName = reportNameEdit.getText().toString().trim();

                        if (newReportName.isEmpty()) {
                            reportNameError.setText(R.string.sdr_rrd_error_name_is_empty);
                            reportNameError.setVisibility(View.VISIBLE);
                            return;
                        }

                        String extension = FileUtils.getExtension(selectedFile.getName());
                        String newFileName = newReportName + "." + extension;

                        if (FileUtils.nameContainsReservedChars(newFileName)) {
                            reportNameError.setText(R.string.sdr_rrd_error_characters_not_allowed);
                            reportNameError.setVisibility(View.VISIBLE);
                            return;
                        }

                        File destFile = new File(selectedFile.getParentFile(), newFileName);

                        if (!selectedFile.equals(destFile)) {
                            if (destFile.exists()) {
                                reportNameError.setText(R.string.sdr_rrd_error_report_exists);
                                reportNameError.setVisibility(View.VISIBLE);
                                return;
                            }

                            if (renameSavedReportFile(selectedFile, destFile)) {
                                updateReportsListView();
                            } else {
                                Toast.makeText(SavedReportsActivity.this, R.string.sdr_t_report_renaming_error, Toast.LENGTH_SHORT).show();
                            }
                        }

                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    private Dialog createReportDeletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sdr_drd_title);
        builder.setMessage(R.string.sdr_drd_msg);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(R.string.spm_delete_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (selectedFile.isDirectory()) {
                    FileUtils.deleteFilesInDirectory(selectedFile);
                }

                if (selectedFile.delete()) {
                    updateReportsListView();
                } else {
                    Toast.makeText(SavedReportsActivity.this, R.string.sdr_t_report_deletion_error, Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private void openReportFile(File reportFile) {
        File reportOutputFile = new File(reportFile, reportFile.getName());
        String fileName = reportOutputFile.getName();
        String baseName = FileUtils.getBaseName(fileName);
        String extension = FileUtils.getExtension(fileName).toLowerCase();
        Uri reportOutputPath = Uri.fromFile(reportOutputFile);

        if ("HTML".equalsIgnoreCase(extension)) {
            // run the html report viewer
            Intent htmlViewer = new Intent();
            htmlViewer.setClass(SavedReportsActivity.this, SavedReportHtmlViewerActivity.class);
            htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_URI, reportOutputPath.toString());
            htmlViewer.putExtra(BaseHtmlViewerActivity.EXTRA_RESOURCE_LABEL, baseName);
            startActivity(htmlViewer);
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
                Toast.makeText(SavedReportsActivity.this,
                        getString(R.string.sdr_t_no_app_available, extension), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateReportsListView() {
        File[] files = getSavedReportsDir().listFiles();

        if (files != null && files.length > 0) {
            FileArrayAdapter arrayAdapter = new FileArrayAdapter(this, files);
            arrayAdapter.sort(new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });
            setListAdapter(arrayAdapter);
        } else {
            nothingToDisplayText.setText(R.string.r_browser_nothing_to_display);
        }
    }

    private boolean renameSavedReportFile(File srcFile, File destFile) {
        // rename base file
        boolean result = srcFile.renameTo(destFile);
        // rename sub-files
        if (result && destFile.isDirectory()) {
            String srcName = srcFile.getName();
            String destName = destFile.getName();

            FilenameFilter reportNameFilter = new ReportFilenameFilter(srcName);
            File[] subFiles = destFile.listFiles(reportNameFilter);
            for (File subFile : subFiles) {
                File newSubFile = new File(subFile.getParentFile(), destName);
                result &= subFile.renameTo(newSubFile);
            }
        }

        return result;
    }

    private File getSavedReportsDir() {
        File appFilesDir = (FileUtils.isExternalStorageWritable()) ? getExternalFilesDir(null) : getFilesDir();
        File savedReportsDir = new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);

        if (!savedReportsDir.exists() && !savedReportsDir.mkdirs()){
            Ln.e("Unable to create %s", savedReportsDir);
        }

        return savedReportsDir;
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private static class ReportFilenameFilter implements FilenameFilter {

        private String reportName;

        private ReportFilenameFilter(String reportName) {
            this.reportName = reportName;
        }

        @Override
        public boolean accept(File dir, String filename) {
            return filename.equals(reportName);
        }

    }
}
