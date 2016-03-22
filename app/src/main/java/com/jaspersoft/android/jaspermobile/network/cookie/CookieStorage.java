package com.jaspersoft.android.jaspermobile.network.cookie;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface CookieStorage extends java.net.CookieStore {
    @NonNull
    org.apache.http.client.CookieStore getApacheCookieStore();
}
