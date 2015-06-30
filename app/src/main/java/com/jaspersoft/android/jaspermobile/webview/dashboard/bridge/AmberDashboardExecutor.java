/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.util.ScreenUtil;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil_;
import com.jaspersoft.android.jaspermobile.webview.dashboard.flow.WebFlowFactory;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class AmberDashboardExecutor extends AbstractDashboardExecutor {
    private final WebView webView;
    private final ResourceLookup resource;

    private AmberDashboardExecutor(WebView webView, ResourceLookup resource) {
        this.webView = webView;
        this.resource = resource;
    }

    public static DashboardExecutor newInstance(WebView webView, ResourceLookup resource) {
        if (webView == null) {
            throw new IllegalArgumentException("WebView should not be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("ResourceLookup should not be null");
        }
        return new AmberDashboardExecutor(webView, resource);
    }

    @Override
    void doPreparation() {
        WebFlowFactory.getInstance(webView.getContext())
                .createFlow(resource)
                .load(webView);
    }

    @Override
    void doExecution() {
        ScreenUtil screenUtil = ScreenUtil_.getInstance_(webView.getContext());
        String runScript = String.format(
                "javascript:MobileDashboard.configure({\"diagonal\": \"%s\"}).run()",
                screenUtil.getDiagonal());
        webView.loadUrl(runScript);
    }
}
