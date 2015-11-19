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

package com.jaspersoft.android.jaspermobile.network;


import com.jaspersoft.android.jaspermobile.R;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public enum ExceptionRule {
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), R.string.error_http_400),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), R.string.error_http_401),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), R.string.error_http_403),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), R.string.error_http_404),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), R.string.error_http_500),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY.value(), R.string.error_http_502),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE.value(), R.string.error_http_503),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT.value(), R.string.error_http_504);

    private final int httpStatus;
    private final int message;

    ExceptionRule(int httpStatus, int message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getMessage() {
        return message;
    }

    public static Map<Integer, ExceptionRule> all() {
        Map<Integer, ExceptionRule> collection =
                new HashMap<Integer, ExceptionRule>();
        for (ExceptionRule rule : values()) {
            collection.put(rule.getHttpStatus(), rule);
        }
        return collection;
    }
}
