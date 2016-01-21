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
    private final List<JasperRequestInterceptor> mRequestInterceptors;
    private final List<UrlPolicy> mUrlPolicies;
    private final JasperWebViewClientListener mJasperWebViewClientListener;

    private SystemWebViewClient(JasperWebViewClientListener jasperWebViewClientListener,
                                List<JasperRequestInterceptor> requestInterceptors,
                                List<UrlPolicy> urlPolicies) {
        mJasperWebViewClientListener = jasperWebViewClientListener;
        mRequestInterceptors = requestInterceptors;
        mUrlPolicies = urlPolicies;
    }

    public Builder newBuilder() {
        Builder builder = new Builder().withDelegateListener(mJasperWebViewClientListener);
        for (JasperRequestInterceptor requestInterceptor : mRequestInterceptors) {
            builder.registerInterceptor(requestInterceptor);
        }
        for (UrlPolicy urlPolicy : mUrlPolicies) {
            builder.registerUrlPolicy(urlPolicy);
        }
        return builder;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        boolean defaultResult = super.shouldOverrideUrlLoading(view, url);
        for (UrlPolicy urlPolicy : mUrlPolicies) {
            defaultResult |= urlPolicy.shouldOverrideUrlLoading(view, url);
        }
        return defaultResult;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mJasperWebViewClientListener.onPageStarted(url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mJasperWebViewClientListener.onPageFinishedLoading(url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        mJasperWebViewClientListener.onReceivedError(errorCode, description, failingUrl);
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
        for (JasperRequestInterceptor requestInterceptor : mRequestInterceptors) {
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

    public static class Builder {
        private final List<JasperRequestInterceptor> requestInterceptors = new ArrayList<>();
        private final List<UrlPolicy> urlPolicies = new ArrayList<>();

        private JasperWebViewClientListener jasperWebViewClientListener;

        public Builder withDelegateListener(JasperWebViewClientListener jasperWebViewClientListener) {
            this.jasperWebViewClientListener = jasperWebViewClientListener;
            return this;
        }

        public Builder registerInterceptor(JasperRequestInterceptor requestInterceptor) {
            requestInterceptors.add(requestInterceptor);
            return this;
        }

        public Builder registerUrlPolicy(UrlPolicy urlPolicy) {
            urlPolicies.add(urlPolicy);
            return this;
        }

        public SystemWebViewClient build() {
            if (jasperWebViewClientListener == null) {
                jasperWebViewClientListener = new EmptyWebClientCallbackDelegate();
            }
            return new SystemWebViewClient(jasperWebViewClientListener, requestInterceptors, urlPolicies);
        }
    }
}
