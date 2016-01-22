package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

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

    @NonNull
    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeEvents;
    }

    @NonNull
    @Override
    public VisualizeComponent run(@NonNull String jsonParams) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @NonNull
    @Override
    public VisualizeComponent loadPage(String page) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @NonNull
    @Override
    public VisualizeComponent update(@NonNull String jsonParams) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @NonNull
    @Override
    public VisualizeComponent refresh() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
