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

package com.jaspersoft.android.jaspermobile.util;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ScrollableTitleHelper {

    @RootContext
    protected ActionBarActivity activity;

    public void injectTitle(CharSequence title) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setTitle(title);

        TextView toolBarTitle = null;
        ViewGroup toolBar = (ViewGroup) activity.findViewById(R.id.tb_navigation);
        if (toolBar == null) return;

        int toolbarChildCount = toolBar.getChildCount();
        for (int i = 0; i < toolbarChildCount; i++) {
            View view = toolBar.getChildAt(i);
            if (view instanceof TextView) {
                toolBarTitle = (TextView) view;
                break;
            }
        }

        if (toolBarTitle == null) return;
        toolBar.removeView(toolBarTitle);

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View scrollContainer = layoutInflater.inflate(R.layout.scrollable_title_container,
                null, false);
        ViewGroup container = (ViewGroup) scrollContainer.findViewById(R.id.container);
        container.addView(toolBarTitle);

        toolBar.addView(scrollContainer);
    }

}
