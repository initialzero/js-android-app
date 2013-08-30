/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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
import android.util.Base64;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.HashMap;

/**
 * @author Ivan Gadzhega
 * @since 1.4
 */
public abstract class BaseHtmlViewerActivity extends RoboActivity {

    // Extras
    public static final String EXTRA_RESOURCE_URL = "BaseHtmlViewerActivity.EXTRA_RESOURCE_URL";

    @InjectView(R.id.htmlViewer_webView)                protected WebView webView;
    @InjectView(R.id.htmlViewer_webView_progressBar)    protected ProgressBar progressBar;

    @Inject
    protected JsRestClient jsRestClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_viewer_layout);

        prepareWebView();
        setWebViewClient();

        //get resource url from the intent extras
        String url = getIntent().getExtras().getString(EXTRA_RESOURCE_URL);

        // basic auth
        HashMap<String, String> map = new HashMap<String, String>();
        JsServerProfile serverProfile = jsRestClient.getServerProfile();
        String authorisation = serverProfile.getUsernameWithOrgId() + ":" + serverProfile.getPassword();
        String encodedAuthorisation = "Basic " + Base64.encodeToString(authorisation.getBytes(), Base64.NO_WRAP);
        map.put("Authorization", encodedAuthorisation);
        // load url
        webView.loadUrl(url, map);
    }

    protected void prepareWebView() {
        webView.getSettings().setPluginsEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
    }

    protected void setWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // hide progress bar after page load
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
