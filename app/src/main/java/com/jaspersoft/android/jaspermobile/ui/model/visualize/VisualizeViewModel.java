/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.ui.model.visualize;

import android.support.annotation.NonNull;

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
        WebViewEvents webViewEvents = new RxWebViewEvents(configuration);
        WebViewComponent webViewComponent = new SimpleWebViewComponent(webViewEvents);
        VisualizeEvents visualizeEvents = new RxVisualizeEvents(configuration);
        VisualizeComponent visualizeComponent = new SimpleVisualizeComponent(
                configuration.getWebView(), visualizeEvents);
        return new VisualizeViewModel(visualizeComponent, webViewComponent);
    }

    @NonNull
    @Override
    public VisualizeEvents visualizeEvents() {
        return mVisualizeComponentDelegate.visualizeEvents();
    }

    @NonNull
    @Override
    public VisualizeComponent run(@NonNull VisualizeExecOptions options) {
        return mVisualizeComponentDelegate.run(options);
    }

    @NonNull
    @Override
    public VisualizeComponent loadPage(String page) {
        return mVisualizeComponentDelegate.loadPage(page);
    }

    @NonNull
    @Override
    public VisualizeComponent update(@NonNull String jsonParams) {
        return mVisualizeComponentDelegate.update(jsonParams);
    }

    @NonNull
    @Override
    public VisualizeComponent refresh() {
        return mVisualizeComponentDelegate.refresh();
    }

    @Override
    public WebViewEvents webViewEvents() {
        return mWebViewComponentDelegate.webViewEvents();
    }
}
