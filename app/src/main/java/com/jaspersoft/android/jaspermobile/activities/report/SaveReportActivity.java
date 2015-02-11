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

package com.jaspersoft.android.jaspermobile.activities.report;

import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.report.fragment.SaveItemFragment;
import com.jaspersoft.android.jaspermobile.activities.report.fragment.SaveItemFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.ArrayList;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.8
 */
@EActivity
public class SaveReportActivity extends RoboSpiceActivity {

    @Extra
    String requestId;
    @Extra
    ResourceLookup resource;
    @Extra
    ArrayList<ReportParameter> reportParameters;
    @Extra
    int pageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SaveItemFragment saveItemFragment = SaveItemFragment_.builder()
                    .requestId(requestId)
                    .resource(resource)
                    .reportParameters(reportParameters)
                    .pageCount(pageCount)
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, saveItemFragment, SaveItemFragment.TAG)
                    .commit();
        }
    }

}
