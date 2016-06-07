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

package com.jaspersoft.android.jaspermobile.network.cookie;

import org.apache.http.cookie.Cookie;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class ApacheCookieStore implements org.apache.http.client.CookieStore {
    private final CookieStore mCookieStore;
    private final CookieMapper mCookieMapper;

    public ApacheCookieStore(CookieStore cookieStore, CookieMapper cookieMapper) {
        mCookieStore = cookieStore;
        mCookieMapper = cookieMapper;
    }

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public List<Cookie> getCookies() {
        List<HttpCookie> cookies = mCookieStore.getCookies();
        List<Cookie> result = new ArrayList<>(cookies.size());
        for (HttpCookie cookie : cookies) {
            result.add(mCookieMapper.toApacheCookie(cookie));
        }
        return result;
    }

    @Override
    public boolean clearExpired(Date date) {
        return false;
    }

    @Override
    public void clear() {
    }
}
