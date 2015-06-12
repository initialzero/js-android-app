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

package com.jaspersoft.android.jaspermobile.network;


import com.jaspersoft.android.jaspermobile.R;

import org.springframework.http.HttpStatus;

import java.util.EnumMap;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public enum ExceptionRule {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, R.string.error_http_400),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, R.string.error_http_401),
    FORBIDDEN(HttpStatus.FORBIDDEN, R.string.error_http_403),
    NOT_FOUND(HttpStatus.NOT_FOUND, R.string.error_http_404),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, R.string.error_http_500),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, R.string.error_http_502),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, R.string.error_http_503),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, R.string.error_http_504);

    private final HttpStatus httpStatus;
    private final int message;

    ExceptionRule(HttpStatus httpStatus, int message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getMessage() {
        return message;
    }

    public static EnumMap<HttpStatus, ExceptionRule> all() {
        EnumMap<HttpStatus, ExceptionRule> collection =
                new EnumMap<HttpStatus, ExceptionRule>(HttpStatus.class);
        for (ExceptionRule rule : values()) {
            collection.put(rule.getHttpStatus(), rule);
        }
        return collection;
    }
}
