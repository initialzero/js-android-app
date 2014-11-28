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
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FilterFavoritesDialogFragment extends DialogFragment {
    public static final String TAG = FilterFavoritesDialogFragment.class.getSimpleName();
    private static final int NO_FILTER_POSITION = 0;
    private static final int BY_REPORTS_POSITION = 1;
    private static final int BY_DASHBOARDS_POSITION = 2;
    private static final int BY_FOLDER_POSITION = 3;

    @FragmentArg
    ResourceLookup.ResourceType mType;

    private FilterFavoritesDialogListener filterSelectedListener;

    public static void show(FragmentManager fm, ResourceLookup.ResourceType mType, FilterFavoritesDialogListener filterSelectedListener) {
        FilterFavoritesDialogFragment dialogFragment =
                (FilterFavoritesDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = FilterFavoritesDialogFragment_.builder()
                    .mType(mType)
                    .build();
            dialogFragment.setFilterSelectedListener(filterSelectedListener);
            dialogFragment.show(fm ,TAG);
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
                getString(R.string.s_fd_option_dashboards),
                getString(R.string.f_fd_option_folders)
        };

        int position = NO_FILTER_POSITION;

        if(mType != null) {
            if (mType.equals(ResourceLookup.ResourceType.reportUnit)) {
                position = BY_REPORTS_POSITION;
            } else if (mType.equals(ResourceLookup.ResourceType.dashboard)) {
                position = BY_DASHBOARDS_POSITION;
            } else if (mType.equals(ResourceLookup.ResourceType.folder)) {
                position = BY_FOLDER_POSITION;
            }
        }

        builder.setSingleChoiceItems(options, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BY_REPORTS_POSITION:
                        mType = ResourceLookup.ResourceType.reportUnit;
                        break;
                    case BY_DASHBOARDS_POSITION:
                        mType = ResourceLookup.ResourceType.dashboard;
                        break;
                    case BY_FOLDER_POSITION:
                        mType = ResourceLookup.ResourceType.folder;
                        break;
                    default:
                        mType = null;
                        break;
                }

                if (filterSelectedListener != null) {
                    filterSelectedListener.onDialogPositiveClick(mType);
                }
                dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void setFilterSelectedListener(FilterFavoritesDialogListener filterSelectedListener) {
        this.filterSelectedListener = filterSelectedListener;
    }

    public static interface FilterFavoritesDialogListener {
        void onDialogPositiveClick(ResourceLookup.ResourceType type);
    }
}
