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

package com.jaspersoft.android.jaspermobile.dialog;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import eu.inmite.android.lib.dialogs.ProgressDialogFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SingleProgressDialogFragment extends ProgressDialogFragment {

    public static final String TAG = SingleProgressDialogFragment.class.getSimpleName();

    //---------------------------------------------------------------------
    // Static methods
    //---------------------------------------------------------------------

    public static ProgressDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
        ProgressDialogBuilder2 builder =  new ProgressDialogBuilder2(context, fragmentManager);
        builder.setTag(TAG);
        return builder;
    }

    public static void dismiss(FragmentManager fragmentManager) {
        ProgressDialogFragment progressDialog = (ProgressDialogFragment)
                fragmentManager.findFragmentByTag(TAG);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    public static class ProgressDialogBuilder2 extends ProgressDialogBuilder {
        protected ProgressDialogBuilder2(Context context, FragmentManager fragmentManager) {
            super(context, fragmentManager);
        }
    }

}
