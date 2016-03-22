package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class DeleteOptionRequest {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mOptionId;

    public DeleteOptionRequest(@NonNull String uri, @NonNull String optionId) {
        mUri = uri;
        mOptionId = optionId;
    }

    @NonNull
    public String getOptionId() {
        return mOptionId;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }
}
