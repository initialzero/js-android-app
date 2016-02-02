package com.jaspersoft.android.jaspermobile.network.cookie;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.CookieManager;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class WebViewCookieStoreLollipop implements WebViewCookieStore {
    private final CookieManager mCookieManager;

    WebViewCookieStoreLollipop(CookieManager cookieManager) {
        mCookieManager = cookieManager;
    }

    @Override
    public void add(@NonNull String domain, @NonNull String cookie) {
        mCookieManager.setCookie(domain, cookie);
        mCookieManager.flush();
    }

    @Override
    public void removeCookie(@NonNull String domain) {
        mCookieManager.setCookie(domain, null);
        mCookieManager.flush();
    }

    @Override
    public void removeAllCookies() {
        mCookieManager.removeAllCookies(null);
        mCookieManager.flush();
    }
}
