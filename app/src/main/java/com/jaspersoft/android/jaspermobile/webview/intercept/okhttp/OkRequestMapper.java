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

package com.jaspersoft.android.jaspermobile.webview.intercept.okhttp;

import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;

import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.5
 */
class OkRequestMapper {
    private static final String PRAGMA = "Pragma";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String USER_AGENT = "User-Agent";
    private static final String[] HEADERS = new String[] {PRAGMA, CACHE_CONTROL, USER_AGENT};

    @Nullable
    public Request toOkHttpRequest(WebRequest request) {
        String url = request.getUrl();
        HttpUrl proxyUrl = HttpUrl.parse(url);
        if (proxyUrl == null) {
            return null;
        }

        Request.Builder requestBuilder = new Request.Builder()
                .get()
                .url(proxyUrl);

        Map<String, String> requestHeaders = extractRequestHeaders(request);
        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        return requestBuilder.build();
    }

    private Map<String, String> extractRequestHeaders(WebRequest request) {
        Map<String, String> requestHeaders = request.getRequestHeaders();
        filterHeaders(requestHeaders);
        return requestHeaders;
    }

    private void filterHeaders(Map<String, String> requestHeaders) {
        for (String header : HEADERS) {
            requestHeaders.remove(header);
        }
    }
}
