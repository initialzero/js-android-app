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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import org.androidannotations.annotations.FragmentArg;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class RenameDialogFragment extends DialogFragment implements DialogInterface.OnShowListener, View.OnClickListener {

    public static final String TAG = RenameDialogFragment.class.getSimpleName();

    @FragmentArg
    File selectedFile;

    private AlertDialog mDialog;
    private EditText reportNameEdit;
    private OnRenamedAction onRenamedActionListener;

    public static void show(FragmentManager fm, File file, OnRenamedAction onRenamedAction) {
        RenameDialogFragment dialogFragment = (RenameDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = RenameDialogFragment_.builder().selectedFile(file).build();
            dialogFragment.setRenamedActionListener(onRenamedAction);
            dialogFragment.show(fm, TAG);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View customLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.rename_report_dialog_layout, null);
        reportNameEdit = (EditText) customLayout.findViewById(R.id.report_name_input);
        reportNameEdit.setText(FileUtils.getBaseName(selectedFile.getName()));
        reportNameEdit.setSelection(reportNameEdit.getText().length());

        reportNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reportNameEdit.setError(null);
            }
        });

        // inflate custom layout
        builder.setView(customLayout);
        // define title
        builder.setTitle(R.string.sdr_rrd_title);
        // define actions
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        mDialog = builder.create();
        mDialog.setOnShowListener(this);
        return mDialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(reportNameEdit, 0);
    }

    @Override
    public void onClick(View v) {
        String newReportName = reportNameEdit.getText().toString().trim();

        if (newReportName.isEmpty()) {
            reportNameEdit.setError(getString(R.string.sdr_rrd_error_name_is_empty));
            return;
        }

        String extension = FileUtils.getExtension(selectedFile.getName());
        String newFileName = newReportName + "." + extension;

        if (FileUtils.nameContainsReservedChars(newFileName)) {
            reportNameEdit.setError(getString(R.string.sdr_rrd_error_characters_not_allowed));
            return;
        }

        File destFile = new File(selectedFile.getParentFile(), newFileName);

        if (!selectedFile.equals(destFile)) {
            if (destFile.exists()) {
                reportNameEdit.setError(getString(R.string.sdr_rrd_error_report_exists));
                return;
            }

            if (renameSavedReportFile(selectedFile, destFile)) {
                if (onRenamedActionListener != null) {
                    onRenamedActionListener.onRenamed();
                }
            } else {
                Toast.makeText(getActivity(), R.string.sdr_t_report_renaming_error, Toast.LENGTH_SHORT).show();
            }
        }

        dismiss();
    }

    public void setRenamedActionListener(OnRenamedAction onRenamedActionListener) {
        this.onRenamedActionListener = onRenamedActionListener;
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

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public static interface OnRenamedAction {
        void onRenamed();
    }

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
