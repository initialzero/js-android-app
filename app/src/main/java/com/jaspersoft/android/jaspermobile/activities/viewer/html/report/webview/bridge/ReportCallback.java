package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.bridge;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface ReportCallback {
    void onScriptLoaded();
    void onLoadStart();
    void onLoadDone(String parameters);
    void onLoadError(String error);
    void onTotalPagesLoaded(int pages);
    void onPageChange(int page);
    void onRemoteCall(String type, String location);
}
