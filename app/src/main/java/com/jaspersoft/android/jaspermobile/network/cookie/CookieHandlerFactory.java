package com.jaspersoft.android.jaspermobile.network.cookie;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.CookieSyncManager;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CookieHandlerFactory {
    @Inject
    public CookieHandlerFactory() {
    }

    @NonNull
    public CookieHandler newStore(@NonNull Context context) {
        WebViewCookieStore webViewCookieStore = createWebViewCookieStore(context);
        java.net.CookieStore cookieStore = new PersistentCookieStore(context);

        CookieMapper cookieMapper = new CookieMapper();
        org.apache.http.client.CookieStore apacheStore = new ApacheCookieStore(cookieStore, cookieMapper);

        AppCookieStore appCookieStore = new AppCookieStore(webViewCookieStore, cookieStore, apacheStore);

        CookieHandler defaultCookieManager = new CookieManager(appCookieStore, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(defaultCookieManager);

        return defaultCookieManager;
    }

    private WebViewCookieStore createWebViewCookieStore(@NonNull Context context) {
        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new WebViewCookieStoreLollipop(cookieManager);
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            return new WebViewCookieStorePreLollipop(cookieManager, cookieSyncManager);
        }
    }
}
