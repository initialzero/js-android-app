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
import android.text.Selection;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

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
    int minValue;
    @FragmentArg
    int maxValue;
    @FragmentArg
    int value;

    public int mValue;

    private OnPageSelectedListener onPageSelectedListener;

    public static Builder builder(FragmentManager fragmentManager) {
        return new Builder(fragmentManager);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ViewGroup customView = (ViewGroup) LayoutInflater.from(getActivity())
                .inflate(R.layout.number_dialog_layout, (ViewGroup) getView(), false);
        final NumberPicker numberPicker = (NumberPicker)
                customView.findViewById(R.id.numberPicker);
        mValue = numberPicker.getValue();

        int inputId = getActivity().getResources().getIdentifier("numberpicker_input", "id", "android");
        final EditText editText = (EditText) numberPicker.findViewById(inputId);
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
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);

        builder.setTitle(R.string.dialog_current_page);
        builder.setView(customView);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Selection.removeSelection(editText.getText());
                    dispatchOnPageSelected();
                }
                return false;
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dispatchOnPageSelected();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                numberPicker.setValue(value);
            }
        });

        return dialog;
    }

    private void dispatchOnPageSelected() {
        if (onPageSelectedListener != null) {
            onPageSelectedListener.onPageSelected(mValue);
        }
        dismiss();
    }

    public void setPageSelectedListener(OnPageSelectedListener onPageSelectedListener) {
        this.onPageSelectedListener = onPageSelectedListener;
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public static class Builder {
        private int value;
        private int minValue;
        private int maxValue;
        private OnPageSelectedListener onPageSelectedListener;
        private final FragmentManager fragmentManager;

        public Builder(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }

        public Builder value(int value) {
            this.value = value;
            return this;
        }

        public Builder minValue(int minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder maxValue(int maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public Builder selectListener(OnPageSelectedListener onPageSelectedListener) {
            this.onPageSelectedListener = onPageSelectedListener;
            return this;
        }

        public void show() {
            NumberDialogFragment dialogFragment = (NumberDialogFragment)
                    fragmentManager.findFragmentByTag(TAG);

            if (dialogFragment == null) {
                dialogFragment = NumberDialogFragment_.builder()
                        .minValue(minValue)
                        .maxValue(maxValue)
                        .value(value)
                        .build();
                dialogFragment.setPageSelectedListener(onPageSelectedListener);
                dialogFragment.show(fragmentManager, TAG);
            }
        }
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
