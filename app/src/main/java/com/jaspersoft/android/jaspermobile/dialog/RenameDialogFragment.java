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

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class RenameDialogFragment extends BaseDialogFragment implements DialogInterface.OnShowListener {

    private final static String SELECTED_FILE_ARG = "selected_file";
    private final static String EXTENSION_ARG = "extension_arg";
    private final static String RECORD_URI_ARG = "record_uri";

    private File selectedFile;
    private String extension;
    private Uri recordUri;

    private AlertDialog mDialog;
    private EditText reportNameEdit;

    @SystemService
    protected InputMethodManager inputMethodManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View customLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.rename_report_dialog_layout, null);

        String reportName = FileUtils.getBaseName(selectedFile.getName());
        reportNameEdit = (EditText) customLayout.findViewById(R.id.report_name_input);
        reportNameEdit.setText(reportName);
        reportNameEdit.setSelection(reportNameEdit.getText().length());
        reportNameEdit.addTextChangedListener(new RenameTextWatcher());

        builder.setView(customLayout);
        builder.setTitle(R.string.sdr_rrd_title);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        mDialog = builder.create();
        mDialog.setOnShowListener(this);
        return mDialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnPositiveClickListener());
        inputMethodManager.showSoftInput(reportNameEdit, 0);
    }

    @Override
    protected Class<RenameDialogClickListener> getDialogCallbackClass() {
        return RenameDialogClickListener.class;
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

    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(SELECTED_FILE_ARG)) {
                Uri fileUri = args.getParcelable(SELECTED_FILE_ARG);
                selectedFile = new File(fileUri.getPath());
            }
            if (args.containsKey(EXTENSION_ARG)) {
                extension = args.getString(EXTENSION_ARG);
            }
            if (args.containsKey(RECORD_URI_ARG)) {
                String recordUriString = args.getString(RECORD_URI_ARG);
                recordUri = Uri.parse(recordUriString);
            }
        }
    }

    public static RenameDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new RenameDialogFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class RenameDialogFragmentBuilder extends BaseDialogFragmentBuilder<RenameDialogFragment> {

        public RenameDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public RenameDialogFragmentBuilder setSelectedFile(File file) {
            args.putParcelable(SELECTED_FILE_ARG, Uri.fromFile(file));
            return this;
        }

        public RenameDialogFragmentBuilder setExtension(String extension) {
            args.putString(EXTENSION_ARG, extension);
            return this;
        }

        public RenameDialogFragmentBuilder setRecordUri(Uri recordUri) {
            args.putString(RECORD_URI_ARG, recordUri.toString());
            return this;
        }

        @Override
        protected RenameDialogFragment build() {
            return new RenameDialogFragment_();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface RenameDialogClickListener extends DialogClickListener {
        void onRenamed(String newFileName, String newFilePath, Uri recordUri);
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

    private class OnPositiveClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String reportName = reportNameEdit.getText().toString().trim();
            String newReportFolderName = reportNameEdit.getText().toString().trim() + "." + extension;

            if (newReportFolderName.isEmpty()) {
                reportNameEdit.setError(getString(R.string.sdr_rrd_error_name_is_empty));
                return;
            }

            if (FileUtils.nameContainsReservedChars(newReportFolderName)) {
                reportNameEdit.setError(getString(R.string.sdr_rrd_error_characters_not_allowed));
                return;
            }

            File destFile = new File(selectedFile.getParentFile(), newReportFolderName);
            if (destFile.exists()) {
                reportNameEdit.setError(getString(R.string.sdr_rrd_error_report_exists));
            } else {
                if (renameSavedReportFile(selectedFile, destFile)) {
                    if (mDialogListener != null) {
                        String newFilePath = new File(destFile, newReportFolderName).getPath();
                        ((RenameDialogClickListener) mDialogListener).onRenamed(reportName, newFilePath, recordUri);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.sdr_t_report_renaming_error, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        }
    }

    private class RenameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            reportNameEdit.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
