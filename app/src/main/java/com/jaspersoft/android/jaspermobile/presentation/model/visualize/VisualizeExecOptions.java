package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class VisualizeExecOptions {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mParams;
    @NonNull
    private final AppCredentials mAppCredentials;
    private final double mDiagonal;

    public VisualizeExecOptions(@NonNull String uri,
                                @NonNull String params,
                                @NonNull AppCredentials appCredentials,
                                double diagonal) {
        mUri = uri;
        mParams = params;
        mAppCredentials = appCredentials;
        mDiagonal = diagonal;
    }

    @NonNull
    public String getParams() {
        return mParams;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    @NonNull
    public AppCredentials getAppCredentials() {
        return mAppCredentials;
    }

    public double getDiagonal() {
        return mDiagonal;
    }
}
