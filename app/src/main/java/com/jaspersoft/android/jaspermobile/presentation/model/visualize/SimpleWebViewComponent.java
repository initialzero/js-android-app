package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class SimpleWebViewComponent implements WebViewComponent {
    @NonNull
    private final WebViewConfiguration mWebViewConfiguration;

    public SimpleWebViewComponent(@NonNull WebViewConfiguration webViewConfiguration) {
        mWebViewConfiguration = webViewConfiguration;
    }

    @Override
    public WebViewEvents webViewEvents() {
        return new RxWebViewEvents(mWebViewConfiguration);
    }
}
