/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.legacy;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * For description of flow refer to http://code2flow.com/uyFdCJ
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class TokenHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final String COOKIE = "Cookie";

    private final Context mContext;

    public TokenHttpRequestInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        JasperAccountManager manager = JasperAccountManager.get(mContext);

        String token = manager.getActiveAuthToken();
        request.getHeaders().add(COOKIE, token);
        ClientHttpResponse response = execution.execute(request, body);
        HttpStatus status = response.getStatusCode();

        // Token expired
        if (status == HttpStatus.UNAUTHORIZED) {
            manager.invalidateToken(token);
            token = manager.getActiveAuthToken();
            request.getHeaders().add(COOKIE, token);
            response = execution.execute(request, body);
        }

        return response;
    }

}
