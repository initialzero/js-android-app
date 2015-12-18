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
import com.google.inject.Singleton;
import com.jaspersoft.android.sdk.network.Cookies;
import com.jaspersoft.android.sdk.service.token.TokenCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CountDownLatch;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
final class SessionCache implements TokenCache {
    private CookieManager mCookieManager;
    private CookieSyncManager mCookieSyncManager;

    @Inject
    Context mContext;

    @Inject
    private SessionCache() {
    }

    @Nullable
    @Override
    public Cookies get(@NotNull String host) {
        String cookie = getCookieManager().getCookie(host);
        if (cookie == null) {
            return null;
        }
        return Cookies.parse(cookie);
    }

    @Override
    public void put(@NotNull String host, @NotNull Cookies cookies) {
        for (String cookie : cookies.get()) {
            getCookieManager().setCookie(host, cookie);
        }
        syncCookies();
    }

    @Override
    public void remove(@NotNull String key) {
        removeCookies();
        syncCookies();
    }

    private void removeCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            getCookieManager().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Timber.e(e, "Failed to remove cookies from cache");
            }
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
