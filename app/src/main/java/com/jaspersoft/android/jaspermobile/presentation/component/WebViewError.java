package com.jaspersoft.android.jaspermobile.presentation.component;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class WebViewError {
    @NonNull
    private final String title;
    @NonNull
    private final String message;

    public WebViewError(@NonNull String title, @NonNull String message) {
        this.title = title;
        this.message = message;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public String getTitle() {
        return title;
    }
}
