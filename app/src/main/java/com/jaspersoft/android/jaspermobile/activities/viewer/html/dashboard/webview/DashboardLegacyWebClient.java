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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview;

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jaspersoft.android.jaspermobile.webview.dashboard.script.ScriptTagFactory;

import java.io.IOException;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class DashboardLegacyWebClient extends WebViewClient {
    public static final String INJECTION_TOKEN = "**injection**";

    private final WebViewClient mDecoratedClient;

    public DashboardLegacyWebClient(WebViewClient decoratedClient) {
        mDecoratedClient = decoratedClient;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return mDecoratedClient.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        view.loadUrl("javascript:" + ScriptTagFactory.getInstance(view.getContext()).getTagCreator().createTag());
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse response = super.shouldInterceptRequest(view, url);
        return hackAssetLoading(view, response, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest webResourceRequest) {
        WebResourceResponse response = super.shouldInterceptRequest(view, webResourceRequest);
        String url = webResourceRequest.getUrl().toString();
        return hackAssetLoading(view, response, url);
    }

    private WebResourceResponse hackAssetLoading(WebView view, WebResourceResponse response, String url) {
        if (url != null && url.contains(INJECTION_TOKEN)) {
            String assetPath = url.substring(
                    url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length(), url.length());
            try {
                response = new WebResourceResponse(
                        "application/javascript",
                        "UTF8",
                        view.getContext().getAssets().open(assetPath)
                );
            } catch (IOException e) {
                Timber.e(e, "Failed to load asset by path: " + assetPath);
            }
        }
        return response;
    }
}
