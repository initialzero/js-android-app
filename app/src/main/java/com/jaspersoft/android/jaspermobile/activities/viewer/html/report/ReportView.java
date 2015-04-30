package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface ReportView {
    void showEmptyView();
    void hideEmptyView();
    void showErrorView(CharSequence error);
    void hideErrorView();
}
