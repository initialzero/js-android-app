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

package com.jaspersoft.android.jaspermobile.visualize;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EBean
public class HyperlinkHelper {
    @RootContext
    protected ActionBarActivity activity;
    @Inject
    protected JsRestClient jsRestClient;

    @AfterInject
    final void inject() {
        final RoboInjector injector = RoboGuice.getInjector(activity);
        injector.injectMembersWithoutViews(this);
    }

    @UiThread
    public void executeReport(String data) {
        ReportData reportData = new Gson().fromJson(data, ReportData.class);
        ProgressDialogFragment
                .builder(activity.getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .show();
        requestResourceLookup(reportData);
    }

    @Background
    protected void requestResourceLookup(ReportData reportData) {
        try {
            ResourceLookup lookup = jsRestClient.getReportResource(reportData.getResource());

            ArrayList<ReportParameter> reportParameters = transform(reportData.getParams());
            startReportActivity(lookup, reportParameters);
        } catch (HttpStatusCodeException exception) {
            reportError(exception);
        }
    }

    @NonNull
    private ArrayList<ReportParameter> transform(Map<String, Set<String>> params) {
        ArrayList<ReportParameter> collection = new ArrayList<ReportParameter>();
        for (Map.Entry<String, Set<String>> entry : params.entrySet()) {
            ReportParameter reportParameter = new ReportParameter();
            reportParameter.setName(entry.getKey());
            reportParameter.setValues(entry.getValue());
            collection.add(reportParameter);
        }
        return collection;
    }

    @UiThread
    protected void startReportActivity(@NonNull ResourceLookup lookup, ArrayList<ReportParameter> reportParameters) {
        ProgressDialogFragment.dismiss(activity.getSupportFragmentManager());
//        ReportViewerActivity_
//                .intent(activity)
//                .reportParameters(reportParameters)
//                .resource(lookup)
//                .start();
    }

    @UiThread
    protected void reportError(HttpStatusCodeException exception) {
        Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
        ProgressDialogFragment.dismiss(activity.getSupportFragmentManager());
    }
}
