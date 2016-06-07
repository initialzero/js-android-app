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

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
final class WebViewCookieStorePreLollipop extends WebViewCookieStore {
    private final CookieManager mCookieManager;
    private final CookieSyncManager mCookieSyncManager;

    public WebViewCookieStorePreLollipop(CookieManager cookieManager,
                                         CookieSyncManager cookieSyncManager) {
        mCookieManager = cookieManager;
        mCookieSyncManager = cookieSyncManager;
    }

    @Override
    public void add(@NonNull String domain, @NonNull String cookie) {
        super.add(domain, cookie);
        mCookieManager.setCookie(domain, cookie);
        mCookieSyncManager.sync();
    }

    @Override
    public void removeCookie(@NonNull String domain) {
        super.removeCookie(domain);
        mCookieManager.setCookie(domain, null);
        mCookieSyncManager.sync();
    }

    @Override
    public void removeAllCookies() {
        super.removeAllCookies();
        mCookieManager.removeAllCookie();
        mCookieSyncManager.sync();
    }
}
