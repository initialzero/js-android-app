package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;
import android.webkit.WebView;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SimpleVisualizeComponent implements VisualizeComponent {
    @NonNull
    private final VisualizeEvents mVisualizeEvents;
    @NonNull
    private final WebView mWebView;

    public SimpleVisualizeComponent(@NonNull WebView webView,
                                    @NonNull VisualizeEvents visualizeEvents) {
        mWebView = webView;
        mVisualizeEvents = visualizeEvents;
    }

    @NonNull
    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeEvents;
    }

    @NonNull
    @Override
    public VisualizeComponent run(@NonNull VisualizeExecOptions options) {
        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileReport.configure")
                .append("({})")
                .append(".run({")
                .append("\"uri\": \"%s\",")
                .append("\"params\": %s")
                .append("})");
        String executeScript = String.format(builder.toString(),
                options.getUri(),
                options.getParams()
        );
        mWebView.loadUrl(executeScript);
        return this;
    }

    @NonNull
    @Override
    public VisualizeComponent loadPage(String page) {
        String executeScript = String.format("javascript:MobileReport.selectPage(%s)", page);
        mWebView.loadUrl(executeScript);
        return this;
    }

    @NonNull
    @Override
    public VisualizeComponent update(@NonNull String jsonParams) {
        String executeScript = String.format("javascript:MobileReport.applyReportParams(%s)", jsonParams);
        mWebView.loadUrl(executeScript);
        return this;
    }

    @NonNull
    @Override
    public VisualizeComponent refresh() {
        mWebView.loadUrl("javascript:MobileReport.refresh()");
        return this;
    }
}
