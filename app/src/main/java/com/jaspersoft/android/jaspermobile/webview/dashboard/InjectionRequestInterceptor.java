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

package com.jaspersoft.android.jaspermobile.webview.dashboard;

import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;
import com.jaspersoft.android.jaspermobile.webview.WebResponse;
import com.jaspersoft.android.jaspermobile.webview.intercept.WebResourceInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class InjectionRequestInterceptor implements WebResourceInterceptor {
    public static final String INJECTION_TOKEN = "**injection**";

    private InjectionRequestInterceptor() {
    }

    private static class InstanceHolder {
        private static final InjectionRequestInterceptor INSTANCE = new InjectionRequestInterceptor();
    }

    public static InjectionRequestInterceptor getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public WebResponse interceptRequest(final WebView view, WebRequest request) {
        String url = request.getUrl();
        boolean canIntercept = url.contains(INJECTION_TOKEN);

        if (canIntercept) {
            try {
                return intercept(view, url);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @NonNull
    private WebResponse intercept(WebView view, String url) throws IOException {
        final String assetPath = url.substring(
                url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length(), url.length());
        final InputStream data = view.getContext().getAssets().open(assetPath);
        return new WebResponse() {
            @Override
            public String getMimeType() {
                return "application/javascript";
            }

            @Override
            public String getEncoding() {
                return "UTF8";
            }

            @Override
            public InputStream getData() {
                return data;
            }

            @Override
            public int getStatusCode() {
                return 200;
            }

            @Override
            public String getReasonPhrase() {
                return "OK";
            }

            @Override
            public Map<String, String> getResponseHeaders() {
                return Collections.emptyMap();
            }
        };
    }
}
