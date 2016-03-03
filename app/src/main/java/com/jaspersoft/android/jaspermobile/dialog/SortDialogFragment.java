/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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
import android.support.v7.view.ContextThemeWrapper;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.sorting.SortOrder;

import org.androidannotations.annotations.EFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SortDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener{

    private static final String SORT_ORDER_ARG = "sort_order";

    private static final int BY_LABEL = 0;
    private static final int BY_CREATION_DATE = 1;

    private SortOrder mSortOrder;

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
        if (mSortOrder.equals(SortOrder.LABEL)) {
            position = BY_LABEL;
        }
        if (mSortOrder.equals(SortOrder.CREATION_DATE)) {
            position = BY_CREATION_DATE;
        }

        builder.setSingleChoiceItems(options, position, this);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BY_LABEL:
                mSortOrder = SortOrder.LABEL;
                break;
            case BY_CREATION_DATE:
                mSortOrder = SortOrder.CREATION_DATE;
                break;
            default:
                mSortOrder = SortOrder.LABEL;
                break;
        }

        if (mDialogListener != null) {
            ((SortDialogClickListener) mDialogListener).onOptionSelected(mSortOrder);
        }
        dismiss();
    }

    @Override
    protected Class<SortDialogClickListener> getDialogCallbackClass() {
        return SortDialogClickListener.class;
    }

    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(SORT_ORDER_ARG)) {
                mSortOrder = (SortOrder) args.getSerializable(SORT_ORDER_ARG);
            }
        }
    }

    public static SortDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new SortDialogFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class SortDialogFragmentBuilder extends BaseDialogFragmentBuilder<SortDialogFragment> {

        public SortDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public SortDialogFragmentBuilder setInitialSortOption(SortOrder sortOrder) {
            args.putSerializable(SORT_ORDER_ARG, sortOrder);
            return this;
        }

        @Override
        protected SortDialogFragment build() {
            return new SortDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface SortDialogClickListener extends DialogClickListener {
        void onOptionSelected(SortOrder sortOrder);
    }

}
