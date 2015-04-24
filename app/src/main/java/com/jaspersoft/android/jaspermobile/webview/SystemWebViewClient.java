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

package com.jaspersoft.android.jaspermobile.webview;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class SystemWebViewClient extends WebViewClient {
    private JasperWebViewClientListener jasperWebViewClientListener;
    private final List<JasperRequestInterceptor> requestInterceptors;

    private SystemWebViewClient() {
        this.jasperWebViewClientListener = new EmptyWebClientCallbackDelegate();
        this.requestInterceptors = new ArrayList<JasperRequestInterceptor>();
    }

    private SystemWebViewClient(JasperWebViewClientListener jasperWebViewClientListener) {
        this.jasperWebViewClientListener = jasperWebViewClientListener;
        this.requestInterceptors = new ArrayList<JasperRequestInterceptor>();
    }

    public static SystemWebViewClient newInstance() {
        return new SystemWebViewClient();
    }


    public SystemWebViewClient withDelegateListener(JasperWebViewClientListener jasperWebViewClientListener) {
        this.jasperWebViewClientListener = jasperWebViewClientListener;
        return this;
    }

    public SystemWebViewClient withInterceptor(JasperRequestInterceptor requestInterceptor) {
        registerInterceptor(requestInterceptor);
        return this;
    }

    public void registerInterceptor(JasperRequestInterceptor requestInterceptor) {
        requestInterceptors.add(requestInterceptor);
    }

    public void unregisterInterceptor(JasperRequestInterceptor requestInterceptor) {
        requestInterceptors.remove(requestInterceptor);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        jasperWebViewClientListener.onPageStarted(url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        jasperWebViewClientListener.onPageFinishedLoading(url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        jasperWebViewClientListener.onReceivedError(errorCode, description, failingUrl);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        WebResourceResponse response = super.shouldInterceptRequest(view, url);
        return tryToInterceptRequests(view, url, response);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest webResourceRequest) {
        WebResourceResponse response = super.shouldInterceptRequest(view, webResourceRequest);
        String url = webResourceRequest.getUrl().toString();
        return tryToInterceptRequests(view, url, response);
    }

    private WebResourceResponse tryToInterceptRequests(WebView view, String url, WebResourceResponse response) {
        for (JasperRequestInterceptor requestInterceptor : requestInterceptors) {
            if (requestInterceptor.canIntercept(url)) {
                WebResourceResponse interceptedResponse = requestInterceptor.interceptRequest(view, response, url);
                if (interceptedResponse != null) {
                    return interceptedResponse;
                }
            }
        }
        return response;
    }

    private static class EmptyWebClientCallbackDelegate implements JasperWebViewClientListener {
        @Override
        public void onPageStarted(String newUrl) {
        }

        @Override
        public void onReceivedError(int errorCode, String description, String failingUrl) {
        }

        @Override
        public void onPageFinishedLoading(String url) {
        }
    }
}
