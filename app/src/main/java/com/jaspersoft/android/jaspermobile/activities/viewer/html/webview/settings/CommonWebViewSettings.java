package com.jaspersoft.android.jaspermobile.activities.viewer.html.webview.settings;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class CommonWebViewSettings implements WebViewSettings {
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void setup(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
    }
}
