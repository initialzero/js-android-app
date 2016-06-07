/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.ui.model.visualize;

import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SimpleVisualizeComponent implements VisualizeComponent {
    @NonNull
    private final VisualizeEvents mVisualizeEvents;
    @NonNull
    private final WebView mWebView;

    public SimpleVisualizeComponent(@NonNull WebView webView,
                                    @NonNull VisualizeEvents visualizeEvents) {
        mWebView = webView;
        mVisualizeEvents = visualizeEvents;
    }

    @NonNull
    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeEvents;
    }

    @NonNull
    @Override
    public VisualizeComponent run(@NonNull VisualizeExecOptions options) {
        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileReport.configure")
                .append("({ \"auth\": ")
                .append("{")
                .append("\"username\": \"%s\",")
                .append("\"password\": \"%s\",")
                .append("\"organization\": \"%s\"")
                .append("}, ")
                .append("\"diagonal\": %s ")
                .append("})")
                .append(".run({")
                .append("\"uri\": \"%s\",")
                .append("\"params\": %s")
                .append("})");
        AppCredentials credentials = options.getAppCredentials();
        String executeScript = String.format(builder.toString(),
                credentials.getUsername(),
                credentials.getPassword(),
                credentials.getOrganization(),
                options.getDiagonal(),
                options.getUri(),
                options.getParams()
        );
        mWebView.loadUrl(executeScript);
        return this;
    }

    @NonNull
    @Override
    public VisualizeComponent loadPage(String page) {
        String executeScript = String.format("javascript:MobileReport.selectPage(%s)", page);
        mWebView.loadUrl(executeScript);
        return this;
    }

    @NonNull
    @Override
    public VisualizeComponent update(@NonNull String jsonParams) {
        String executeScript = String.format("javascript:MobileReport.applyReportParams(%s)", jsonParams);
        mWebView.loadUrl(executeScript);
        return this;
    }

    @NonNull
    @Override
    public VisualizeComponent refresh() {
        mWebView.loadUrl("javascript:MobileReport.refresh()");
        return this;
    }
}
