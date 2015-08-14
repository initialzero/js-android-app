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

package com.jaspersoft.android.jaspermobile.webview.dashboard;

import android.support.annotation.Nullable;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.JasperRequestInterceptor;

import java.io.IOException;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class InjectionRequestInterceptor implements JasperRequestInterceptor {
    public static final String INJECTION_TOKEN = "**injection**";

    public InjectionRequestInterceptor() {}

    @Nullable
    @Override
    public WebResourceResponse interceptRequest(WebView view, WebResourceResponse response, String url) {
        String assetPath = url.substring(
                url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length(), url.length());
        try {
            response = new WebResourceResponse(
                    "application/javascript",
                    "UTF8",
                    view.getContext().getAssets().open(assetPath)
            );
        } catch (IOException e) {
            response = null;
            Timber.e(e, "Failed to load asset by path: " + assetPath);
        }

        return response;
    }

    @Override
    public boolean canIntercept(String url) {
        return (url != null && url.contains(INJECTION_TOKEN));
    }
}
