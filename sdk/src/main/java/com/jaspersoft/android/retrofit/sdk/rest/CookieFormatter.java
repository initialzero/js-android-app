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

package com.jaspersoft.android.retrofit.sdk.rest;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import retrofit.client.Header;

/**
 * Singleton via enum pattern.
 *
 * @author Tom Koptel
 * @since 2.1
 */
enum CookieFormatter {
    INSTANCE;

    /**
     * Accept raw list of headers from JRS. Then applies join strategy on it.
     *
     * @param headers which contain cookie data
     * @return cookie as string instance
     */
    public static String format(List<Header> headers) {
        return INSTANCE.performFormat(headers);
    }

    String performFormat(List<Header> headers) {
        List<Header> cookies = filterCookieHeaders(headers);
        StringBuilder stringBuilder = joinCookieHeaders(cookies);
        appendTimeZone(stringBuilder);

        return stringBuilder.toString();
    }

    void appendTimeZone(StringBuilder stringBuilder) {
        TimeZone timeZone = TimeZone.getDefault();
        stringBuilder.append(";userTimezone=").append(timeZone.getID());
    }

    StringBuilder joinCookieHeaders(List<Header> cookies) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<Header> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            Header cookie = iterator.next();
            stringBuilder.append(cookie.getValue());
            if (iterator.hasNext()) {
                stringBuilder.append(";");
            }
        }
        return stringBuilder;
    }

    List<Header> filterCookieHeaders(List<Header> headers) {
        List<Header> cookies = new ArrayList<Header>();
        for (Header header : headers) {
            if (!TextUtils.isEmpty(header.getName()) && header.getName().equals("Set-Cookie")) {
                cookies.add(header);
            }
        }
        return cookies;
    }

}
