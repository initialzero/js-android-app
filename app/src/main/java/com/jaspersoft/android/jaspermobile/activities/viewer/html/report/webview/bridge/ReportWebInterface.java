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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.bridge;

import android.webkit.JavascriptInterface;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportWebInterface implements ReportCallback {

    private final ReportCallback decoratedCallback;

    public ReportWebInterface(ReportCallback decoratedCallback) {
        this.decoratedCallback = decoratedCallback;
    }

    @JavascriptInterface
    @Override
    public void onScriptLoaded() {
        decoratedCallback.onScriptLoaded();
    }

    @JavascriptInterface
    @Override
    public void onLoadStart() {
        decoratedCallback.onLoadStart();
    }

    @JavascriptInterface
    @Override
    public void onLoadDone(String parameters) {
        decoratedCallback.onLoadDone(parameters);
    }

    @JavascriptInterface
    @Override
    public void onLoadError(String error) {
        decoratedCallback.onLoadError(error);
    }

    @JavascriptInterface
    @Override
    public void onTotalPagesLoaded(int pages) {
        decoratedCallback.onTotalPagesLoaded(pages);
    }

    @JavascriptInterface
    @Override
    public void onPageChange(int page) {
        decoratedCallback.onPageChange(page);
    }

    @JavascriptInterface
    @Override
    public void onReferenceClick(String type) {
        decoratedCallback.onReferenceClick(type);
    }

    @JavascriptInterface
    @Override
    public void onReportExecutionClick(String report, String params) {
        decoratedCallback.onReportExecutionClick(report, params);
    }

}
