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

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOptions;
import com.jaspersoft.android.jaspermobile.activities.repository.support.SortOrder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SortDialogFragment extends DialogFragment {

    private static final String TAG = SortDialogFragment.class.getSimpleName();
    private static final int BY_LABEL = 0;
    private static final int BY_CREATION_DATE = 1;

    @Bean
    SortOptions sortOptions;

    private SortDialogListener sortOptionSelectionListener;

    public static void show(FragmentManager fm, SortDialogListener sortDialogListener) {
        SortDialogFragment dialogFragment =
                (SortDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = SortDialogFragment_.builder().build();
            dialogFragment.setSortOptionSelectionListener(sortDialogListener);
            dialogFragment.show(fm ,TAG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.s_ab_sort_by);

        CharSequence[] options = {
                getString(R.string.s_fd_sort_label),
                getString(R.string.s_fd_sort_date)
        };

        int position = 0;
        SortOrder sortOrder = sortOptions.getOrder();
        if (sortOrder.equals(SortOrder.LABEL)) {
            position = BY_LABEL;
        }
        if (sortOrder.equals(SortOrder.CREATION_DATE)) {
            position = BY_CREATION_DATE;
        }

        builder.setSingleChoiceItems(options, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SortOrder sortOrder;
                switch (which) {
                    case BY_LABEL:
                        sortOrder = SortOrder.LABEL;
                        break;
                    case BY_CREATION_DATE:
                        sortOrder = SortOrder.CREATION_DATE;
                        break;
                    default:
                        sortOrder = SortOrder.LABEL;
                        break;
                }
                sortOptions.putOrder(sortOrder);
                if (sortOptionSelectionListener != null) {
                    sortOptionSelectionListener.onOptionSelected(sortOrder);
                }
                dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void setSortOptionSelectionListener(SortDialogListener filterSelectedListener) {
        this.sortOptionSelectionListener = filterSelectedListener;
    }

    public static interface SortDialogListener {
        void onOptionSelected(SortOrder sortOrder);
    }

}
