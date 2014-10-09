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

package com.jaspersoft.android.jaspermobile.activities.viewer.html;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.JsSpiceManager;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.octo.android.robospice.SpiceManager;

import java.util.HashMap;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @since 1.4
 */
@Deprecated
public abstract class BaseHtmlViewerActivity extends RoboActivity {

    // Extras
    public static final String EXTRA_RESOURCE_URI = "BaseHtmlViewerActivity.EXTRA_RESOURCE_URI";
    public static final String EXTRA_RESOURCE_LABEL = "BaseHtmlViewerActivity.EXTRA_RESOURCE_LABEL";

    @Inject
    protected JsRestClient jsRestClient;
    @InjectView(R.id.webViewPlaceholder)
    protected FrameLayout webViewPlaceholder;
    @InjectView(R.id.htmlViewer_webView_progressBar)
    protected ProgressBar progressBar;
    protected JSWebView webView;

    protected String resourceUri;
    protected String resourceLabel;

    @Inject
    private JsSpiceManager jsSpiceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_viewer_layout);

        initDataFromExtras();

        if (getActionBar() != null) {
            getActionBar().setTitle(resourceLabel);
        }

        initWebView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (webView != null) webViewPlaceholder.removeView(webView);
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.html_viewer_layout);
        initWebView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    protected void initDataFromExtras() {
        Bundle extras = getIntent().getExtras();
        resourceUri = extras.getString(EXTRA_RESOURCE_URI);
        resourceLabel = extras.getString(EXTRA_RESOURCE_LABEL);
    }

    protected void loadUrl(String url) {
        // basic auth
        HashMap<String, String> map = new HashMap<String, String>();
        JsServerProfile serverProfile = jsRestClient.getServerProfile();
        String authorisation = serverProfile.getUsernameWithOrgId() + ":" + serverProfile.getPassword();
        String encodedAuthorisation = "Basic " + Base64.encodeToString(authorisation.getBytes(), Base64.NO_WRAP);
        map.put("Authorization", encodedAuthorisation);
        // load url
        webView.loadUrl(url, map);
    }

    protected abstract void loadDataToWebView();

    @Override
    protected void onStart() {
        if (!jsSpiceManager.isStarted())
        jsSpiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (jsSpiceManager.isStarted())
            jsSpiceManager.shouldStop();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return jsSpiceManager;
    }

    //---------------------------------------------------------------------
    // Options Menu
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void initWebView() {
        // create new if necessary
        if (webView == null) createWebView();
        // attach to placeholder
        webViewPlaceholder.addView(webView);
    }

    private void createWebView() {
        webView = new JSWebView(this, null, R.style.htmlViewer_webView);
        prepareWebView();
        setWebViewClient();
        loadDataToWebView();
    }

    private void setWebViewClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // fade in
                if (progressBar.getProgress() == 0) {
                    AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
                    fadeInAnimation.setDuration(500);
                    progressBar.startAnimation(fadeInAnimation);
                }
                // update value
                int maxProgress = progressBar.getMax();
                progressBar.setProgress((maxProgress / 100) * progress);
                // fade out
                if (progress == maxProgress) {
                    AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
                    fadeOutAnimation.setDuration(1000);
                    progressBar.startAnimation(fadeOutAnimation);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void prepareWebView() {
        // disable hardware acceleration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // configure additional settings
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
    }

}
