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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;

import roboguice.fragment.RoboFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.html_viewer_layout)
public class WebViewFragment extends RoboFragment {

    public static final String TAG = WebViewFragment.class.getSimpleName();

    @ViewById
    FrameLayout webViewPlaceholder;
    @ViewById(R.id.htmlViewer_webView_progressBar)
    ProgressBar progressBar;

    @FragmentArg
    String resourceUri;
    @FragmentArg
    String resourceLabel;

    @InstanceState
    boolean mResourceLoaded;
    @InstanceState
    String currentUrl;

    @Inject
    protected JsRestClient jsRestClient;
    @Bean
    ScrollableTitleHelper scrollableTitleHelper;

    private OnWebViewCreated onWebViewCreated;
    private JSWebView webView;

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @AfterViews
    final void init() {
        initWebView();

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            scrollableTitleHelper.injectTitle(getActivity(), resourceLabel);
        }
    }

    private void initWebView() {
        // create new if necessary
        if (webView == null) createWebView();
        // attach to placeholder
        webViewPlaceholder.addView(webView);
    }

    @OptionsItem(android.R.id.home)
    final void goHome() {
        getActivity().onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (webView != null) webViewPlaceholder.removeView(webView);
        super.onConfigurationChanged(newConfig);
        initWebView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    public void loadUrl(String url) {
        // basic auth
        HashMap<String, String> map = Maps.newHashMap();
        JsServerProfile serverProfile = jsRestClient.getServerProfile();
        String authorisation = serverProfile.getUsernameWithOrgId() + ":" + serverProfile.getPassword();
        String encodedAuthorisation = "Basic " + Base64.encodeToString(authorisation.getBytes(), Base64.NO_WRAP);
        map.put("Authorization", encodedAuthorisation);
        // load url
        currentUrl = url;
        webView.loadUrl(url, map);
    }

    public void setOnWebViewCreated(OnWebViewCreated onWebViewCreated) {
        this.onWebViewCreated = onWebViewCreated;
    }

    public boolean isResourceLoaded() {
        return mResourceLoaded;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void createWebView() {
        webView = new JSWebView(getActivity(), null, R.style.htmlViewer_webView);
        prepareWebView();
        setWebViewClient();
        if (onWebViewCreated != null) {
            onWebViewCreated.onWebViewCreated(this);
        }
    }

    private void setWebViewClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // fade in
                if (progressBar.getAlpha() == 0) {
                    ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f)
                            .setDuration(500).start();
                }
                // update value
                int maxProgress = progressBar.getMax();
                progressBar.setProgress((maxProgress / 100) * progress);
                // fade out
                if (progress == maxProgress) {
                    mResourceLoaded = true;
                    ObjectAnimator.ofFloat(progressBar, "alpha", 1f, 0f)
                            .setDuration(1000).start();
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
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
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
    }

    public void refresh() {
        loadUrl(currentUrl);
    }

    public interface OnWebViewCreated {
        void onWebViewCreated(WebViewFragment webViewFragment);
    }

}
