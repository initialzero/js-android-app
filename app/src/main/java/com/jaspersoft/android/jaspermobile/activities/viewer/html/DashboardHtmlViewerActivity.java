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

package com.jaspersoft.android.jaspermobile.activities.viewer.html;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment.WebViewFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment.WebViewFragment_;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectView;

/**
 * Activity that performs dashboard viewing in HTML format.
 *
 * @author Ivan Gadzhega
 * @since 1.4
 */
@EActivity
public class DashboardHtmlViewerActivity extends RoboFragmentActivity
        implements WebViewFragment.OnWebViewCreated {
    @Inject
    JsRestClient jsRestClient;

    @Extra
    String resourceUri;
    @Extra
    String resourceLabel;

    @InjectView(R.id.htmlViewer_layout)
    protected RelativeLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            WebViewFragment webViewFragment = WebViewFragment_.builder()
                    .resourceLabel(resourceLabel).resourceUri(resourceUri).build();
            webViewFragment.setOnWebViewCreated(this);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, webViewFragment, WebViewFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onWebViewCreated(WebViewFragment webViewFragment) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.htmlViewer_layout);
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        int padding_2dp = (int) (2 * scale + 0.5f);
        // set padding in dp for layout
        layout.setPadding(padding_2dp, padding_2dp, padding_2dp, padding_2dp);

        String dashboardUrl = jsRestClient.getServerProfile().getServerUrl()
                + "/flow.html?_flowId=dashboardRuntimeFlow&viewAsDashboardFrame=true&dashboardResource="
                + resourceUri;
        webViewFragment.loadUrl(dashboardUrl);
    }
}
