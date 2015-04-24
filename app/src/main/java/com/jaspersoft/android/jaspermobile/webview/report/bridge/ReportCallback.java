package com.jaspersoft.android.jaspermobile.webview.report.bridge;

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
    void onReferenceClick(String location);
    void onReportExecutionClick(String report, String params);
}
