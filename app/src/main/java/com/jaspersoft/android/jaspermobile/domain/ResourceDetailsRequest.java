package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ResourceDetailsRequest {
    @NonNull
    private final String mUri;
    @NonNull
    private final String mType;

    public ResourceDetailsRequest(
            @NonNull String uri,
            @NonNull String type
    ) {
        mUri = uri;
        mType = type;
    }

    @NonNull
    public String getType() {
        return mType;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceDetailsRequest that = (ResourceDetailsRequest) o;

        if (mUri != null ? !mUri.equals(that.mUri) : that.mUri != null) return false;
        return mType != null ? mType.equals(that.mType) : that.mType == null;

    }

    @Override
    public int hashCode() {
        int result = mUri != null ? mUri.hashCode() : 0;
        result = 31 * result + (mType != null ? mType.hashCode() : 0);
        return result;
    }
}
