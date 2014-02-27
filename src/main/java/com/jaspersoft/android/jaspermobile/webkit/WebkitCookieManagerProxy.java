/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.webkit;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Gadzhega
 * @since 1.8
 */
public class WebkitCookieManagerProxy extends CookieManager {

    private android.webkit.CookieManager webkitCookieManager;

    /**
     * Constructs a new cookie manager.
     *
     * The invocation of this constructor is the same as the invocation of
     * CookieManager(null).
     *
     */
    public WebkitCookieManagerProxy() {
        this(null);
    }

    /**
     * Constructs a new cookie manager using a specified cookie policy.
     *
     * @param cookiePolicy a CookiePolicy to be used by cookie manager
     *                     ACCEPT_ORIGINAL_SERVER will be used if the arg is null.
     */
    public WebkitCookieManagerProxy(CookiePolicy cookiePolicy) {
        super(null, cookiePolicy);
        webkitCookieManager = android.webkit.CookieManager.getInstance();
    }

    /**
     * Searches and gets all cookies in the cache by the specified uri in the
     * request header.
     *
     * @param uri the specified uri to search for
     * @param requestHeaders a list of request headers
     *
     * @return a map that record all such cookies, the map is unchangeable
     * @throws IOException
     *             if some error of I/O operation occurs
     */
    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        // make sure our args are valid
        if (uri == null || requestHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        // prepare our response
        Map<String, List<String>> response = new java.util.HashMap<String, List<String>>();
        String cookie = webkitCookieManager.getCookie(uri.toString());
        if (cookie != null) {
            response.put("Cookie", Arrays.asList(cookie));
        }

        return response;
    }

    /**
     * Sets cookies according to uri and responseHeaders
     *
     * @param uri the specified uri
     * @param responseHeaders a list of request headers
     *
     * @throws IOException if some error of I/O operation occurs
     */
    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        // make sure our args are valid
        if (uri == null || responseHeaders == null) return;

        // go over the headers
        for (String headerKey : responseHeaders.keySet()) {
            // ignore headers which aren't cookie related
            if (headerKey == null) continue;
            if (!(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey.equalsIgnoreCase("Set-Cookie"))) continue;
            // process each of the headers
            for (String headerValue : responseHeaders.get(headerKey)) {
                webkitCookieManager.setCookie(uri.toString(), headerValue);
            }
        }
    }

    @Override
    public CookieStore getCookieStore() {
        // we don't want anyone to work with this cookie store directly
        throw new UnsupportedOperationException();
    }

}
