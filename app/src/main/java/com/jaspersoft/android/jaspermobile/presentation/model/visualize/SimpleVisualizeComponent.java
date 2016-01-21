package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SimpleVisualizeComponent implements VisualizeComponent {
    @NonNull
    private final WebViewConfiguration mWebViewConfiguration;
    @NonNull
    private final VisualizeEvents mVisualizeEvents;

    public SimpleVisualizeComponent(@NonNull WebViewConfiguration webViewConfiguration,
                                    @NonNull VisualizeEvents visualizeEvents) {
        mWebViewConfiguration = webViewConfiguration;
        mVisualizeEvents = visualizeEvents;
    }

    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeEvents;
    }

    @Override
    public VisualizeComponent run() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public VisualizeComponent loadPage(int page) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public VisualizeComponent update(List<ReportParameter> parameters) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public VisualizeComponent refresh() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
