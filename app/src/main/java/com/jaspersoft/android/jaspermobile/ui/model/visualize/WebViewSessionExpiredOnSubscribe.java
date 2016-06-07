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

import android.os.Looper;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.DefaultUrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.SystemWebViewClient;
import com.jaspersoft.android.jaspermobile.webview.UrlPolicy;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class WebViewSessionExpiredOnSubscribe implements Observable.OnSubscribe<Void> {
    private final WebViewConfiguration mConfiguration;

    public WebViewSessionExpiredOnSubscribe(WebViewConfiguration configuration) {
        mConfiguration = configuration;
    }

    @Override
    public void call(final Subscriber<? super Void> subscriber) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException(
                    "Must be called from the main thread. Was: " + Thread.currentThread());
        }

        WebView webView = mConfiguration.getWebView();
        String serverUrl = mConfiguration.getServerUrl();

        UrlPolicy defaultPolicy = new DefaultUrlPolicy(serverUrl)
                .withSessionListener(new DefaultUrlPolicy.SessionListener() {
                    @Override
                    public void onSessionExpired() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(null);
                        }
                    }
                });

        SystemWebViewClient systemWebViewClient = mConfiguration.getSystemWebViewClient();
        SystemWebViewClient client = systemWebViewClient.newBuilder()
                .registerUrlPolicy(defaultPolicy)
                .build();
        mConfiguration.setSystemWebViewClient(client);

        WebViewEnvironment.configure(webView)
                .withWebClient(client);
    }
}
