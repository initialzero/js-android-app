package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class AppResource {
    @NonNull
    private final String mLabel;
    @Nullable
    private final String mDescription;
    @NonNull
    private final String mUri;
    @NonNull
    private final ResourceType mResourceType;

    AppResource(@NonNull String label,
                @Nullable String description,
                @NonNull String uri,
                @NonNull ResourceType resourceType) {
        mLabel = label;
        mDescription = description;
        mUri = uri;
        mResourceType = resourceType;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @NonNull
    public String getLabel() {
        return mLabel;
    }

    @NonNull
    public ResourceType getResourceType() {
        return mResourceType;
    }

    @NonNull
    public String getUri() {
        return mUri;
    }

    public static class Builder {
        private String mLabel;
        private String mDescription;
        private String mUri;
        private ResourceType mResourceType;

        public Builder setLabel(String label) {
            mLabel = label;
            return this;
        }

        public Builder setDescription(String description) {
            mDescription = description;
            return this;
        }

        public Builder setUri(String uri) {
            mUri = uri;
            return this;
        }

        public Builder setResourceType(ResourceType resourceType) {
            mResourceType = resourceType;
            return this;
        }

        public AppResource build() {
            return new AppResource(mLabel, mDescription, mUri, mResourceType);
        }
    }
}
