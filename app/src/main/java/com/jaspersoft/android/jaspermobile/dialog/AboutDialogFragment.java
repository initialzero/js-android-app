/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class AboutDialogFragment extends SimpleDialogFragment implements DialogInterface.OnShowListener {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sa_show_about);
        builder.setMessage(R.string.sa_about_info);
        builder.setNeutralButton(R.string.ok, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        View decorView = getDialog().getWindow().getDecorView();
        if (decorView != null) {
            TextView messageText = (TextView) decorView.findViewById(android.R.id.message);
            if (messageText != null) {
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public static AboutDialogFragmentBuilder createBuilder(Context context, FragmentManager fragmentManager) {
        return new AboutDialogFragmentBuilder(context, fragmentManager);
    }

    public static class AboutDialogFragmentBuilder extends SimpleDialogFragmentBuilder<AboutDialogFragment> {

        public AboutDialogFragmentBuilder(Context context, FragmentManager fragmentManager) {
            super(context, fragmentManager);
        }

        @Override
        public AboutDialogFragment build() {
            return new AboutDialogFragment();
        }
    }
}