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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.presenter;

import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardWebClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EBean
public class AmberPresenter implements DashboardCallback, DashboardPresenter {

    @RootContext
    protected Context context;
    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;


    private boolean mMaximized;
    private ResourceLookup mResource;
    protected WebView mWebView;

    @Override
    public void initialize(WebView webView, ResourceLookup resource) {
        this.mWebView = webView;
        this.mResource = resource;
        configureWebView();
    }

    @Override
    public boolean onBackPressed() {
        if (mMaximized && mWebView != null) {
            mWebView.loadUrl("javascript:DashboardWrapper.minimizeDashlet()");
            scrollableTitleHelper.injectTitle(mResource.getLabel());
            return true;
        }
        return false;
    }

    @UiThread
    @Override
    public void onMaximize(String title) {
        mMaximized = true;
        scrollableTitleHelper.injectTitle(title);
    }

    @UiThread
    @Override
    public void onMinimize() {
        mMaximized = false;
    }

    @UiThread
    @Override
    public void onWrapperLoaded() {
        mWebView.loadUrl("javascript:DashboardWrapper.wrapScreen('200%', '200%')");
    }

    @Override
    public void onDashletsLoaded() {
    }

    private void configureWebView() {
        mWebView.setWebViewClient(new DashboardWebClient(jsWebViewClient));
        mWebView.addJavascriptInterface(new DashboardWebInterface(this), "Android");
        mWebView.setInitialScale(2);
    }

}
