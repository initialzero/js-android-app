/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterManagerBean;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FilterDialogFragment extends DialogFragment {
    public static final String TAG = FilterDialogFragment.class.getSimpleName();
    private static final int BY_REPORTS_POSITION = 1;
    private static final int BY_DASHBOARDS_POSITION = 2;

    @Bean
    FilterManagerBean filterManager;

    private ArrayList<String> mFilters;
    private FilterDialogListener filterSelectedListener;

    public static void show(FragmentManager fm, FilterDialogListener filterSelectedListener) {
        FilterDialogFragment dialogFragment =
                (FilterDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = FilterDialogFragment_.builder().build();
            dialogFragment.setFilterSelectedListener(filterSelectedListener);
            dialogFragment.show(fm, TAG);
        }
    }

    public static void attachListener(FragmentManager fm, FilterDialogListener filterSelectedListener) {
        FilterDialogFragment dialogFragment =
                (FilterDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment != null) {
            dialogFragment.setFilterSelectedListener(filterSelectedListener);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.s_ab_filter_by);
        CharSequence[] options = {
                getString(R.string.s_fd_option_all),
                getString(R.string.s_fd_option_reports),
                getString(R.string.s_fd_option_dashboards)
        };

        int position = 0;
        mFilters = filterManager.getFilters();

        if (filterManager.containsOnlyReport()) {
            position = BY_REPORTS_POSITION;
        }
        if (filterManager.containsOnlyDashboard()) {
            position = BY_DASHBOARDS_POSITION;
        }

        builder.setSingleChoiceItems(options, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BY_REPORTS_POSITION:
                        mFilters = filterManager.getReportFilters();
                        break;
                    case BY_DASHBOARDS_POSITION:
                        mFilters = filterManager.getDashboardFilters();
                        break;
                    default:
                        mFilters = filterManager.getFiltersForLibrary();
                        break;
                }
                filterManager.putFilters(mFilters);
                if (filterSelectedListener != null) {
                    filterSelectedListener.onDialogPositiveClick(mFilters);
                }
                dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void setFilterSelectedListener(FilterDialogListener filterSelectedListener) {
        this.filterSelectedListener = filterSelectedListener;
    }

    public interface FilterDialogListener {
        void onDialogPositiveClick(List<String> types);
    }
}
