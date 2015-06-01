package com.jaspersoft.android.jaspermobile.webview.dashboard.bridge;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface DashboardCallback {
    void onMaximizeStart(String title);
    void onMaximizeEnd(String title);
    void onMaximizeFailed(String error);
    void onMinimizeStart();
    void onMinimizeEnd();
    void onMinimizeFailed(String error);
    void onScriptLoaded();
    void onLoadStart();
    void onLoadDone();
    void onLoadError(String error);
    void onReportExecution(String data);
    void onWindowResizeStart();
    void onWindowResizeEnd();
    void onAuthError(String message);
}
