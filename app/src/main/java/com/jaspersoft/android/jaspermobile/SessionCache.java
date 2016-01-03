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

package com.jaspersoft.android.jaspermobile;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jaspersoft.android.sdk.network.InMemoryCookieStore;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
final class SessionCache implements CookieStore {

    private final InMemoryCookieStore mDelegate;
    private CookieManager mCookieManager;
    private CookieSyncManager mCookieSyncManager;

    @Inject
    Context mContext;

    @Inject
    private SessionCache() {
        mDelegate = new InMemoryCookieStore();
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        getCookieManager().setCookie(uri.toString(), cookie.toString());
        mDelegate.add(uri, cookie);
        syncCookies();
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return mDelegate.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return mDelegate.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return mDelegate.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        getCookieManager().setCookie(uri.toString(), cookie.toString());
        boolean result = mDelegate.remove(uri, cookie);
        syncCookies();
        return result;
    }

    @Override
    public boolean removeAll() {
        removeCookies();
        syncCookies();
        return mDelegate.removeAll();
    }

    private void removeCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getCookieManager().removeAllCookies(null);
        } else {
            getCookieManager().removeAllCookie();
        }
    }

    private void syncCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getCookieManager().flush();
        } else {
            getCookieSyncManager().sync();
        }
    }

    private CookieManager getCookieManager() {
        if (mCookieManager == null) {
            mCookieManager = CookieManager.getInstance();
            mCookieManager.setAcceptCookie(true);
        }
        return mCookieManager;
    }

    private CookieSyncManager getCookieSyncManager() {
        if (mCookieSyncManager == null) {
            mCookieSyncManager = CookieSyncManager.createInstance(mContext);
        }
        return mCookieSyncManager;
    }
}
