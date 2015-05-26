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

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.ArrayList;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class FilterFavoritesDialogFragment extends DialogFragment {
    public static final String TAG = FilterFavoritesDialogFragment.class.getSimpleName();
    private static ArrayList<ResourceLookup.ResourceType> availableFilters;

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
            dialogFragment.show(fm, TAG);
        }
    }

    public static void attachListener(FragmentManager fm,
                                      ResourceLookup.ResourceType filterType,
                                      FilterFavoritesDialogListener filterSelectedListener) {
        FilterFavoritesDialogFragment dialogFragment =
                (FilterFavoritesDialogFragment) fm.findFragmentByTag(TAG);
        if (dialogFragment != null) {
            dialogFragment.setType(filterType);
            dialogFragment.setFilterSelectedListener(filterSelectedListener);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Improve: refactor/encapsulate population

        ArrayList<String> availableFilterTitles = new ArrayList<>();
        availableFilters = new ArrayList<>();

        // null value refers to "All" option
        availableFilterTitles.add(getString(R.string.s_fd_option_all));
        availableFilters.add(null);

        availableFilterTitles.add(getString(R.string.s_fd_option_reports));
        availableFilters.add(ResourceLookup.ResourceType.reportUnit);

        if (isServerEditionPro()) {
            availableFilterTitles.add(2, getString(R.string.s_fd_option_dashboards));
            availableFilters.add(2, ResourceLookup.ResourceType.dashboard);
        }

        availableFilterTitles.add(getString(R.string.f_fd_option_folders));
        availableFilters.add(ResourceLookup.ResourceType.folder);

        builder.setTitle(R.string.s_ab_filter_by);

        int position = availableFilters.indexOf(mType);

        builder.setSingleChoiceItems(availableFilterTitles.toArray(new String[availableFilterTitles.size()]), position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mType = availableFilters.get(which);

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

    public void setType(ResourceLookup.ResourceType mType) {
        this.mType = mType;
    }

    public static interface FilterFavoritesDialogListener {
        void onDialogPositiveClick(ResourceLookup.ResourceType type);
    }

    private boolean isServerEditionPro() {
        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        AccountServerData accountServerData = AccountServerData.get(getActivity(), account);

        return accountServerData.getEdition().equals("PRO");
    }
}
