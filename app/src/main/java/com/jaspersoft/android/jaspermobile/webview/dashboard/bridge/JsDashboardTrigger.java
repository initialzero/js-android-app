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

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.webkit.WebView;

/**
 * Introduces hardcoded Javascript calls.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public final class JsDashboardTrigger implements DashboardTrigger {
    private final WebView webView;

    private JsDashboardTrigger(WebView webView) {
        this.webView = webView;
    }

    public static JsDashboardTrigger with(WebView webView) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView reference should not be null");
        }
        return new JsDashboardTrigger(webView);
    }

    public void refreshDashlet() {
        webView.loadUrl(assembleUri("MobileDashboard.refresh()"));
    }

    public void minimizeDashlet() {
        webView.loadUrl(assembleUri("MobileDashboard.minimizeDashlet()"));
    }

    @Override
    public void applyParams(String params) {
        String executeScript = String.format("javascript:MobileDashboard.applyParams(%s)", params);
        webView.loadUrl(executeScript);
    }

    @Override
    public void refreshDashboard() {
        webView.loadUrl(assembleUri("MobileDashboard.refresh()"));
    }

    private String assembleUri(String command) {
        return "javascript:" + command;
    }
}
