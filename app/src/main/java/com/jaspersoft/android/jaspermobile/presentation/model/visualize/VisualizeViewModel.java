package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class VisualizeViewModel implements VisualizeComponent, WebViewComponent {
    @NonNull
    private final VisualizeComponent mVisualizeComponentDelegate;
    @NonNull
    private final WebViewComponent mWebViewComponentDelegate;

    private VisualizeViewModel(@NonNull VisualizeComponent visualizeComponentDelegate,
                               @NonNull WebViewComponent webViewComponentDelegate) {
        mVisualizeComponentDelegate = visualizeComponentDelegate;
        mWebViewComponentDelegate = webViewComponentDelegate;
    }

    @NonNull
    public static VisualizeViewModel newModel(WebViewConfiguration configuration) {
        WebViewComponent webViewComponent = new SimpleWebViewComponent(configuration);
        VisualizeComponent visualizeComponent = new SimpleVisualizeComponent(configuration);
        return new VisualizeViewModel(visualizeComponent, webViewComponent);
    }

    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeComponentDelegate.visualizeEvents();
    }

    @Override
    public void loadPage(int page) {
        mVisualizeComponentDelegate.loadPage(page);
    }

    @Override
    public void update(List<ReportParameter> parameters) {
        mVisualizeComponentDelegate.update(parameters);
    }

    @Override
    public void refresh() {
        mVisualizeComponentDelegate.refresh();
    }

    @Override
    public WebViewEvents webViewEvents() {
        return mWebViewComponentDelegate.webViewEvents();
    }
}
