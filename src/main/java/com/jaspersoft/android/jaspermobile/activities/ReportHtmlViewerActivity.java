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

package com.jaspersoft.android.jaspermobile.activities;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

/**
 * @author Ivan Gadzhega
 * @since 1.4
 */
public class ReportHtmlViewerActivity extends BaseHtmlViewerActivity {

    @Override
    protected void setWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // hide progress bar after page load
                progressBar.setVisibility(View.GONE);
                // workaround for http://bugzilla.jaspersoft.com/show_bug.cgi?id=29257
                if (jsRestClient.getServerInfo().getVersionCode() < ServerInfo.VERSION_CODES.EMERALD) {
                    webView.clearCache(true);
                }
            }
        });
    }

}
