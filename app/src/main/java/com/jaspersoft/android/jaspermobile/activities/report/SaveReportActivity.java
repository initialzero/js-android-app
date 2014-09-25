/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.report;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Window;

import com.jaspersoft.android.jaspermobile.activities.report.fragment.SaveItemFragment;
import com.jaspersoft.android.jaspermobile.activities.report.fragment.SaveItemFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.WindowFeature;

import java.util.ArrayList;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.8
 */
@EActivity
@WindowFeature({Window.FEATURE_INDETERMINATE_PROGRESS})
public class SaveReportActivity extends RoboSpiceFragmentActivity {

    @Extra
    String resourceLabel;
    @Extra
    String resourceUri;
    @Extra
    ArrayList<ReportParameter> reportParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            SaveItemFragment saveItemFragment = SaveItemFragment_.builder()
                    .resourceLabel(resourceLabel)
                    .resourceUri(resourceUri)
                    .reportParameters(reportParameters)
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, saveItemFragment).commit();
        }
    }

    @OptionsItem(android.R.id.home)
    final void goBack() {
        super.onBackPressed();
    }

}
