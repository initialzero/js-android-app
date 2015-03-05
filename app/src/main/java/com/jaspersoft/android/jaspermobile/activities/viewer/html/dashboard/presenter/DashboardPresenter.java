package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.presenter;

import android.webkit.WebView;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface DashboardPresenter {
    void initialize(WebView webView, ResourceLookup resource);
    boolean onBackPressed();
}
