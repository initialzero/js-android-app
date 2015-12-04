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
import android.webkit.ValueCallback;

import com.google.inject.Inject;
import com.jaspersoft.android.sdk.service.token.InMemoryTokenCache;
import com.jaspersoft.android.sdk.service.token.TokenCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CountDownLatch;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SessionCache implements TokenCache {
    private final TokenCache mCacheDelegate;

    @Inject
    Context mContext;

    @Inject
    private SessionCache() {
        mCacheDelegate = new InMemoryTokenCache();
    }

    @Nullable
    @Override
    public String get(@NotNull String key) {
        return mCacheDelegate.get(key);
    }

    @Override
    public void put(@NotNull String key, @NotNull String token) {
        mCacheDelegate.put(key, token);
        syncCookie(key, token);
    }

    @Override
    public void remove(@NotNull String key) {
        mCacheDelegate.remove(key);
    }

    private void syncCookie(String url, String cookie) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lollyPopSync(url, cookie);
        } else {
            legacySync(url, cookie);
        }
    }

    private void lollyPopSync(final String url, final String cookie) {
        final CookieManager cookieManager = CookieManager.getInstance();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        cookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                cookieManager.setCookie(url,cookie);
                CookieManager.getInstance().flush();
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted during sync", e);
        }
    }

    private void legacySync(String url, String cookie) {
        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        cookieManager.setCookie(url, cookie);
        CookieSyncManager.getInstance().sync();
    }
}
