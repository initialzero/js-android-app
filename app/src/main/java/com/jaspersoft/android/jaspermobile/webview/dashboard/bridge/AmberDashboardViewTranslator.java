/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.dashboard.flow.WebFlowFactory;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class AmberDashboardViewTranslator implements DashboardViewTranslator {
    private final WebView webView;
    private final ResourceLookup resource;
    private boolean mLoaded, mExecuted;

    private AmberDashboardViewTranslator(WebView webView, ResourceLookup resource) {
        this.webView = webView;
        this.resource = resource;
    }

    public static DashboardViewTranslator with(WebView webView, ResourceLookup resource) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView reference should not be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("ResourceLookup reference should not be null");
        }
        return new AmberDashboardViewTranslator(webView, resource);
    }

    @Override
    public void load() {
        Context context = webView.getContext();
        WebFlowFactory.getInstance(context).createFlow(resource).load(webView);
        mLoaded = true;
    }

    @Override
    public void run(double diagonal) {
        String runScript = String.format(
                "javascript:MobileDashboard.configure({\"diagonal\": \"%s\"}).run()",
                diagonal);
        webView.loadUrl(runScript);
        mExecuted = true;
    }

    @Override
    public void pause() {
        if (!mLoaded) {
            throw new IllegalStateException("Dashboard is not loaded. Can't pause.");
        }
        if (!mExecuted) {
            throw new IllegalStateException("Dashboard is not executed. Can't pause.");
        }
        webView.loadUrl(assembleUri("MobileDashboard.pause()"));
    }

    @Override
    public void resume() {
        if (!mLoaded) {
            throw new IllegalStateException("Dashboard is not loaded. Can't resume.");
        }
        if (!mExecuted) {
            throw new IllegalStateException("Dashboard is not executed. Can't resume.");
        }
        webView.loadUrl(assembleUri("MobileDashboard.resume()"));
    }

    private String assembleUri(String command) {
        return "javascript:" + command;
    }
}
