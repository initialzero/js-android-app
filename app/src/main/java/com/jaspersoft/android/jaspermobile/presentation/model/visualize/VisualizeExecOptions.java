package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class VisualizeExecOptions {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mParams;

    public VisualizeExecOptions(@NonNull String uri, @NonNull String params) {
        mUri = uri;
        mParams = params;
    }

    @NonNull
    public String getParams() {
        return mParams;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }
}
