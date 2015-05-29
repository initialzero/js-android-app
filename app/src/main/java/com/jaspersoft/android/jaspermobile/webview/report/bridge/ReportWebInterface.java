/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.webview.report.bridge;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.WebInterface;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportWebInterface extends WebInterface implements ReportCallback {

    private final ReportCallback decoratedCallback;

    private ReportWebInterface(ReportCallback decoratedCallback) {
        this.decoratedCallback = decoratedCallback;
    }

    public static WebInterface from(ReportCallback decoratedCallback) {
        return new ReportWebInterface(decoratedCallback);
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    public void exposeJavascriptInterface(WebView webView) {
        webView.addJavascriptInterface(this, "Android");
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
    public void onReportCompleted(String status, int pages, String errorMessage) {
        decoratedCallback.onReportCompleted(status, pages, errorMessage);
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

    @JavascriptInterface
    @Override
    public void onMultiPageStateObtained(boolean isMultiPage) {
        decoratedCallback.onMultiPageStateObtained(isMultiPage);
    }

}
