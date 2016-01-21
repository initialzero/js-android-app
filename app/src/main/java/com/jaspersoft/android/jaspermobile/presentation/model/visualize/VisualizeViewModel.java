package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;

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
        VisualizeEvents visualizeEvents = null;
        VisualizeComponent visualizeComponent = new SimpleVisualizeComponent(configuration, visualizeEvents);
        return new VisualizeViewModel(visualizeComponent, webViewComponent);
    }

    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeComponentDelegate.visualizeEvents();
    }

    @Override
    public VisualizeComponent run() {
        return mVisualizeComponentDelegate.run();
    }

    @Override
    public VisualizeComponent loadPage(int page) {
        return mVisualizeComponentDelegate.loadPage(page);
    }

    @Override
    public VisualizeComponent update(List<ReportParameter> parameters) {
        return mVisualizeComponentDelegate.update(parameters);
    }

    @Override
    public VisualizeComponent refresh() {
        return mVisualizeComponentDelegate.refresh();
    }

    @Override
    public WebViewEvents webViewEvents() {
        return mWebViewComponentDelegate.webViewEvents();
    }
}
