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

package com.jaspersoft.android.jaspermobile.util.print;

import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.server.ServerInfo;
import com.jaspersoft.android.jaspermobile.util.server.ServerInfoProvider;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;


/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class DashboardPrinter implements ResourcePrinter {
    private WebView webView;
    private ResourceLookup resource;

    private DashboardPrinter() {}

    public static DashboardPrinter get() {
        return new DashboardPrinter();
    }

    public DashboardPrinter withResource(ResourceLookup resource) {
        this.resource = resource;
        return this;
    }

    public DashboardPrinter withWebView(WebView webView) {
        this.webView = webView;
        return this;
    }

    @NonNull
    @Override
    public ResourcePrintJob print() {
        ServerInfoProvider serverInfoProvider = ServerInfo.newInstance(webView.getContext());
        ServerRelease serverRelease = ServerRelease.parseVersion(serverInfoProvider.getServerVersion());

        if (serverRelease.code() >= ServerRelease.AMBER.code()) {
            return DashboardPicturePrintJob.builder()
                    .setPrintName(resource.getLabel())
                    .setWebView(webView)
                    .build()
                    .printResource();
        } else {
            return DashboardWebviewPrintJob.builder()
                    .setPrintName(resource.getLabel())
                    .setWebView(webView)
                    .build()
                    .printResource();
        }
    }
}
