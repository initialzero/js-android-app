package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class SimpleVisualizeComponent implements VisualizeComponent {
    @NonNull
    private final WebViewConfiguration mWebViewConfiguration;

    public SimpleVisualizeComponent(@NonNull WebViewConfiguration webViewConfiguration) {
        mWebViewConfiguration = webViewConfiguration;
    }

    @Override
    public VisualizeEvents visualizeEvents() {
        return null;
    }

    @Override
    public void loadPage(int page) {

    }

    @Override
    public void update(List<ReportParameter> parameters) {

    }

    @Override
    public void refresh() {

    }
}
