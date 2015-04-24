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

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DashboardWebInterface extends WebInterface implements DashboardCallback {
    private final DashboardCallback dashboardCallback;

    private DashboardWebInterface(DashboardCallback dashboardCallback) {
        this.dashboardCallback = dashboardCallback;
    }

    public static void inject(DashboardCallback dashboardCallback, WebView webView) {
        new DashboardWebInterface(dashboardCallback).injectJavascriptInterface(webView);
    }

    @JavascriptInterface
    @Override
    public void onMaximizeStart(String title) {
        dashboardCallback.onMaximizeStart(title);
    }

    @JavascriptInterface
    @Override
    public void onMaximizeEnd(String title) {
        dashboardCallback.onMaximizeEnd(title);
    }

    @JavascriptInterface
    @Override
    public void onMaximizeFailed(String error) {
        dashboardCallback.onMaximizeFailed(error);
    }

    @JavascriptInterface
    @Override
    public void onMinimizeStart() {
        dashboardCallback.onMinimizeStart();
    }

    @JavascriptInterface
    @Override
    public void onMinimizeEnd() {
        dashboardCallback.onMinimizeEnd();
    }

    @JavascriptInterface
    @Override
    public void onMinimizeFailed(String error) {
        dashboardCallback.onMinimizeFailed(error);
    }

    @JavascriptInterface
    @Override
    public void onScriptLoaded() {
        dashboardCallback.onScriptLoaded();
    }

    @JavascriptInterface
    @Override
    public void onLoadStart() {
        dashboardCallback.onLoadStart();
    }

    @JavascriptInterface
    @Override
    public void onLoadDone() {
        dashboardCallback.onLoadDone();
    }

    @JavascriptInterface
    @Override
    public void onLoadError(String error) {
        dashboardCallback.onLoadError(error);
    }

    @JavascriptInterface
    @Override
    public void onReportExecution(String data) {
        dashboardCallback.onReportExecution(data);
    }

    @JavascriptInterface
    @Override
    public void onWindowResizeStart() {
        dashboardCallback.onWindowResizeStart();
    }

    @JavascriptInterface
    @Override
    public void onWindowResizeEnd() {
        dashboardCallback.onWindowResizeEnd();
    }

    @Override
    void injectJavascriptInterface(WebView webView) {
        webView.addJavascriptInterface(this, "Android");
    }
}
