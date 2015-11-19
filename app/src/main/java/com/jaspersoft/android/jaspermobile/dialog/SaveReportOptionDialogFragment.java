/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment
public class SaveReportOptionDialogFragment extends BaseDialogFragment implements DialogInterface.OnShowListener {

    private final static String CURRENTLY_SELECTED_ARG = "currently_selected_report_option";
    private final static String REPORT_OPTIONS_TITLES_ARG = "report_options_titles";

    private AlertDialog saveReportOptionDialog;
    private EditText reportOptionName;
    private TextView nameDuplicationIndicator;

    private int mCurrentlySelected;
    private List<String> mReportOptionsTitles;

    @SystemService
    protected InputMethodManager inputMethodManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View customLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_save_report_option, null);

        reportOptionName = (EditText) customLayout.findViewById(R.id.reportOptionName);
        nameDuplicationIndicator = (TextView) customLayout.findViewById(R.id.reportOptionDuplication);

        if (mCurrentlySelected > 0) {
            String reportOptionName = mReportOptionsTitles.get(mCurrentlySelected);
            this.reportOptionName.setText(reportOptionName);
            checkNameDuplication(reportOptionName);
        }
        reportOptionName.addTextChangedListener(new RenameTextWatcher());
        reportOptionName.setSelection(reportOptionName.getText().length());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(customLayout);
        builder.setTitle(R.string.ro_save_ro);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.sp_save_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reportOptionName = SaveReportOptionDialogFragment.this.reportOptionName.getText().toString();
                if (mDialogListener != null) {
                    ((SaveReportOptionDialogCallback) mDialogListener).onSaveConfirmed(reportOptionName);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        saveReportOptionDialog = builder.create();
        saveReportOptionDialog.setOnShowListener(this);
        return saveReportOptionDialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        inputMethodManager.showSoftInput(reportOptionName, 0);
    }

    @Override
    protected Class<SaveReportOptionDialogCallback> getDialogCallbackClass() {
        return SaveReportOptionDialogCallback.class;
    }

    @Override
    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CURRENTLY_SELECTED_ARG)) {
                mCurrentlySelected = args.getInt(CURRENTLY_SELECTED_ARG);
            }
            if (args.containsKey(REPORT_OPTIONS_TITLES_ARG)) {
                mReportOptionsTitles = args.getStringArrayList(REPORT_OPTIONS_TITLES_ARG);
            }
        }
    }

    public static SaveReportOptionDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new SaveReportOptionDialogFragmentBuilder(fragmentManager);
    }

    private void checkNameDuplication(String name) {
        if (mReportOptionsTitles.contains(name)) {
            nameDuplicationIndicator.setVisibility(View.VISIBLE);
        } else {
            nameDuplicationIndicator.setVisibility(View.GONE);
        }
    }

    private void checkNameIsCorrect(String name) {
        String errorMessage = null;

        if (name.isEmpty()) {
            errorMessage = getString(R.string.sdr_rrd_error_name_is_empty);
        } else if (FileUtils.nameContainsReservedChars(name)) {
            errorMessage = getString(R.string.sdr_rrd_error_characters_not_allowed);
        }

        reportOptionName.setError(errorMessage);
        if (saveReportOptionDialog != null) {
            saveReportOptionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(errorMessage == null);
        }
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class SaveReportOptionDialogFragmentBuilder extends BaseDialogFragmentBuilder<SaveReportOptionDialogFragment> {

        public SaveReportOptionDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public SaveReportOptionDialogFragmentBuilder setCurrentlySelected(int currentlySelected) {
            args.putInt(CURRENTLY_SELECTED_ARG, currentlySelected);
            return this;
        }

        public SaveReportOptionDialogFragmentBuilder setReportOptionsTitles(List<String> reportOptionsTitles) {
            args.putStringArrayList(REPORT_OPTIONS_TITLES_ARG, new ArrayList<>(reportOptionsTitles));
            return this;
        }

        @Override
        protected SaveReportOptionDialogFragment build() {
            return new SaveReportOptionDialogFragment_();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface SaveReportOptionDialogCallback extends DialogClickListener {
        void onSaveConfirmed(String name);
    }

    private class RenameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            reportOptionName.setError(null);
            String reportOptionName = s.toString().trim();

            checkNameDuplication(reportOptionName);
            checkNameIsCorrect(reportOptionName);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
