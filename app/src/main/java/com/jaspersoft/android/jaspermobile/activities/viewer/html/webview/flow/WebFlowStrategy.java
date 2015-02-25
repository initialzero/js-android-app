package com.jaspersoft.android.jaspermobile.activities.viewer.html.webview.flow;

import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface WebFlowStrategy {
    void load(WebView webView, String resourceUri);
}
