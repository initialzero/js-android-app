package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge;

import android.webkit.WebView;

abstract class WebInterface {
    abstract void injectJavascriptInterface(WebView webView);
}
