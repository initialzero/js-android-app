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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.webview;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment(R.layout.scale_dialog)
public class ScaleDialog extends DialogFragment {
    @Pref
    ScalePref_ scalePref;

    @ViewById
    EditText initialScale;
    @ViewById
    EditText dashboardFrameSize;

    @AfterViews
    final void init() {
        String size = scalePref.pageSize().getOr("'100%', '100%'");
        dashboardFrameSize.setText(size);
        initialScale.setText(scalePref.pageScale().get() + "");

        getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                scalePref.pageSize().put(dashboardFrameSize.getText().toString());
                scalePref.pageScale().put(Integer.valueOf(initialScale.getText().toString()));

            }
        });
    }

    @AfterTextChange(R.id.dashboardFrameSize)
    final void afterSizeChanged(Editable text, TextView view) {
        scalePref.pageSize().put(text.toString());
    }

    @AfterTextChange(R.id.initialScale)
    final void afterScaleChanged(Editable text, TextView view) {
        if (!TextUtils.isEmpty(text.toString())) {
            scalePref.pageScale().put(Integer.valueOf(text.toString()));
        }
    }
}
