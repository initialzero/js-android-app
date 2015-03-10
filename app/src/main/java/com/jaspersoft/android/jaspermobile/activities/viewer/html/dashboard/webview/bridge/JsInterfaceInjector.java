package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge;

import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface JsInterfaceInjector {
    void inject(WebView webView, Object bridge);
}
