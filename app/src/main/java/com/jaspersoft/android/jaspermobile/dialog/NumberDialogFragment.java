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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.jaspersoft.android.jaspermobile.R;
import com.negusoft.holoaccent.dialog.AccentAlertDialog;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class NumberDialogFragment extends DialogFragment {

    private static final String TAG = NumberDialogFragment.class.getSimpleName();

    @FragmentArg
    int totalPages;
    @FragmentArg
    int currentPage;

    public int mValue;

    private OnPageSelectedListener onPageSelectedListener;

    public static void show(FragmentManager fm, int currentPage, int totalPages,
                            OnPageSelectedListener onPageSelectedListener) {
        NumberDialogFragment dialogFragment = (NumberDialogFragment)
                fm.findFragmentByTag(TAG);

        if (dialogFragment == null) {
            dialogFragment = NumberDialogFragment_.builder()
                    .totalPages(totalPages).currentPage(currentPage).build();
            dialogFragment.setPageSelectedListener(onPageSelectedListener);
            dialogFragment.show(fm, TAG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AccentAlertDialog.Builder builder = new AccentAlertDialog.Builder(getActivity());

        ViewGroup customView = (ViewGroup) LayoutInflater.from(getActivity())
                .inflate(R.layout.number_dialog_layout, (ViewGroup) getView(), false);
        final NumberPicker numberPicker = (NumberPicker)
                customView.findViewById(R.id.numberPicker);
        mValue = numberPicker.getValue();

        int inputId = getActivity().getResources().getIdentifier("numberpicker_input", "id", "android");
        EditText editText = (EditText) numberPicker.findViewById(inputId);
        editText.addTextChangedListener(new AbstractTextWatcher() {
            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                try {
                    mValue = Integer.valueOf(String.valueOf(sequence));
                } catch (NumberFormatException ex) {
                    // swallow error
                }
            }
        });
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mValue = newVal;
            }
        });
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(totalPages);

        builder.setTitle(R.string.dialog_current_page);
        builder.setView(customView);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onPageSelectedListener != null) {
                    onPageSelectedListener.onPageSelected(mValue);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                numberPicker.setValue(currentPage);
            }
        });

        return dialog;
    }

    public void setPageSelectedListener(OnPageSelectedListener onPageSelectedListener) {
        this.onPageSelectedListener = onPageSelectedListener;
    }

    public static interface OnPageSelectedListener {
        void onPageSelected(int page);
    }

    private static class AbstractTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
