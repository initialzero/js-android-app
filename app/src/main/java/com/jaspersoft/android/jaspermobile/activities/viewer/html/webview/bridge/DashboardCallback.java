package com.jaspersoft.android.jaspermobile.activities.viewer.html.webview.bridge;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface DashboardCallback {
    void onMaximize(String title);
    void onMinimize();
    void onLoaded();
}
