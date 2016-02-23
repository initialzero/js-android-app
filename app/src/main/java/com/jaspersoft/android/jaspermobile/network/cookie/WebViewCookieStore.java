package com.jaspersoft.android.jaspermobile.network.cookie;

import android.support.annotation.NonNull;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
abstract class WebViewCookieStore {

    WebViewCookieStore() {
        Timber.tag("WebViewCookieStore");
    }

    public void add(@NonNull String domain, @NonNull String cookie) {
        Timber.d(String.format("#add domain %s cookie %s", domain, cookie));
    }

    public void removeCookie(@NonNull String domain) {
        Timber.d(String.format("#removeCookie domain %s", domain));
    }

    public void removeAllCookies() {
        Timber.d("#removeAllCookies");
    }
}
