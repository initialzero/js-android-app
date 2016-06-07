/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.ui.view.activity;

import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.ReportCastActivity_;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ReportVisualizeFragment;
import com.jaspersoft.android.jaspermobile.ui.view.fragment.ReportVisualizeFragment_;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@EActivity(R.layout.report_viewer_layout)
public class ReportVisualizeActivity extends CastActivity {
    @Extra
    protected ResourceLookup resource;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            ReportVisualizeFragment viewFragment = ReportVisualizeFragment_.builder()
                    .resource(resource)
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.control, viewFragment, ReportVisualizeFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onCastStarted() {
        ReportCastActivity_.intent(this)
                .resource(resource)
                .start();

        finish();
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_rvs_v);
    }
}
