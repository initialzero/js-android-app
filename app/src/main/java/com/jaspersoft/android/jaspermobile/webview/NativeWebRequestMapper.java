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

package com.jaspersoft.android.jaspermobile.webview;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebResourceRequest;

import java.util.Collections;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.5
 */
class NativeWebRequestMapper {

    @NonNull
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebRequest toGenericRequest(final WebResourceRequest request) {
        return new WebRequest() {
            @NonNull
            @Override
            public String getUrl() {
                return request.getUrl().toString();
            }

            @Nullable
            @Override
            public String getMethod() {
                return request.getMethod();
            }

            @NonNull
            @Override
            public Map<String, String> getRequestHeaders() {
                return request.getRequestHeaders();
            }
        };
    }

    @NonNull
    public WebRequest toGenericRequest(final String url) {
        return new WebRequest() {
            @NonNull
            @Override
            public String getUrl() {
                return url;
            }

            @Nullable
            @Override
            public String getMethod() {
                return null;
            }

            @NonNull
            @Override
            public Map<String, String> getRequestHeaders() {
                return Collections.emptyMap();
            }
        };
    }

}
