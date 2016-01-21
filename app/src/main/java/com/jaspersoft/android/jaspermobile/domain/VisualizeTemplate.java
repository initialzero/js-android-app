package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class VisualizeTemplate {
    @NonNull
    private final String mContent;
    @NonNull
    private final String mServerUrl;

    public VisualizeTemplate(@NonNull String content, @NonNull String serverUrl) {
        mContent = content;
        mServerUrl = serverUrl;
    }

    @NonNull
    public String getContent() {
        return mContent;
    }

    @NonNull
    public String getServerUrl() {
        return mServerUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualizeTemplate that = (VisualizeTemplate) o;

        if (!mContent.equals(that.mContent)) return false;
        return mServerUrl.equals(that.mServerUrl);
    }

    @Override
    public int hashCode() {
        int result = mContent.hashCode();
        result = 31 * result + mServerUrl.hashCode();
        return result;
    }
}
