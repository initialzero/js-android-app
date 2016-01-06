package com.jaspersoft.android.jaspermobile.network.cookie;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
interface WebViewCookieStore {

    void add(@NonNull String domain, @NonNull String cookie);

    void removeCookie(@NonNull String domain);

    void removeAllCookies();
}
