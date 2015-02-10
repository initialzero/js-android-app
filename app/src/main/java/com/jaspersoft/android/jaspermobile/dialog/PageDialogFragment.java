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
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.SystemService;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class PageDialogFragment extends DialogFragment {
    private static final String TAG = PageDialogFragment.class.getSimpleName();

    @FragmentArg
    protected int max;
    @FragmentArg
    protected int min;

    @SystemService
    protected InputMethodManager inputMethodManager;

    private OnPageSelectedListener onPageSelectedListener;

    public static Builder configure() {
        return new Builder();
    }

    public void setPageSelectedListener(OnPageSelectedListener onPageSelectedListener) {
        this.onPageSelectedListener = onPageSelectedListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        ViewGroup customView = (ViewGroup) layoutInflater
                .inflate(R.layout.page_dialog_layout,
                        (ViewGroup) getActivity().getWindow().getDecorView(), false);
        final EditText numberEditText = (EditText) customView.findViewById(R.id.customNumber);
        numberEditText.setFilters(new InputFilter[] {new InputFilterMinMax(min, max)});

        builder.setTitle(R.string.rv_select_page);
        builder.setView(customView);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                numberEditText.requestFocus();
                inputMethodManager.showSoftInput(numberEditText, 0);

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (numberEditText.length() == 0) {
                                    numberEditText.setError(
                                            getString(R.string.sp_error_field_required));
                                } else {
                                    int page;
                                    try {
                                        page = Integer.valueOf(numberEditText.getText().toString());
                                    } catch (NumberFormatException ex) {
                                        page = Integer.MAX_VALUE;
                                        numberEditText.setText(String.valueOf(page));
                                    }

                                    if (onPageSelectedListener != null) {
                                        onPageSelectedListener.onPageSelected(page);
                                        dismiss();
                                    }
                                }
                            }
                        });
            }
        });
        return dialog;
    }

    public static class Builder {
        private OnPageSelectedListener onPageSelectedListener;
        private int min = -1;
        private int max = -1;

        public Builder setOnPageSelectedListener(OnPageSelectedListener onPageSelectedListener) {
            this.onPageSelectedListener = onPageSelectedListener;
            return this;
        }

        public Builder setMin(int min) {
            this.min = min;
            return this;
        }

        public Builder setMax(int max) {
            this.max = max;
            return this;
        }

        public void show(FragmentManager fm) {
            PageDialogFragment dialogFragment = (PageDialogFragment)
                    fm.findFragmentByTag(TAG);

            if (dialogFragment == null) {
                ensureDefaults();
                dialogFragment = PageDialogFragment_.builder()
                        .min(min)
                        .max(max)
                        .build();
                dialogFragment.setPageSelectedListener(onPageSelectedListener);
                dialogFragment.show(fm, TAG);
            }
        }

        private void ensureDefaults() {
            if (min == -1) {
                min = Integer.MIN_VALUE;
            }
            if (max == -1) {
                max = Integer.MAX_VALUE;
            }
        }
    }

    private static class InputFilterMinMax implements InputFilter {
        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}
