/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FilterDialogFragment extends DialogFragment {
    public static final String TAG = FilterDialogFragment.class.getSimpleName();
    private static final int FILTER_BY_REPORTS = 1;
    private static final int FILTER_BY_DASHBOARDS = 2;
    private static final ArrayList<String> ALL_TYPES = new ArrayList<String>(){{
        add(ResourceLookup.ResourceType.reportUnit.toString());
        add(ResourceLookup.ResourceType.dashboard.toString());
    }};

    public ArrayList<String> mTypes = ALL_TYPES;
    private FilterDialogListener filterSelectedListener;

    public static void show(FragmentManager fm, FilterDialogListener filterSelectedListener) {
        FilterDialogFragment dialogFragment =
                (FilterDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = new FilterDialogFragment();
            dialogFragment.setFilterSelectedListener(filterSelectedListener);
            dialogFragment.show(fm ,TAG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.s_ab_filter_by);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (filterSelectedListener != null) {
                    filterSelectedListener.onDialogPositiveClick(mTypes);
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        CharSequence[] options = {
                getString(R.string.s_fd_option_all),
                getString(R.string.s_fd_option_reports),
                getString(R.string.s_fd_option_dashboards)
        };

        builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case FILTER_BY_REPORTS:
                        mTypes = new ArrayList<String>();
                        mTypes.add(ResourceLookup.ResourceType.reportUnit.toString());
                        break;
                    case FILTER_BY_DASHBOARDS:
                        mTypes = new ArrayList<String>();
                        mTypes.add(ResourceLookup.ResourceType.dashboard.toString());
                        break;
                    default:
                        mTypes = ALL_TYPES;
                        break;
                }
            }
        });

        return builder.create();
    }

    public void setFilterSelectedListener(FilterDialogListener filterSelectedListener) {
        this.filterSelectedListener = filterSelectedListener;
    }

    public static interface FilterDialogListener {
        void onDialogPositiveClick(List<String> types);
    }
}
