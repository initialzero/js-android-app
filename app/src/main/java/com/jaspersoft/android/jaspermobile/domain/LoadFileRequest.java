package com.jaspersoft.android.jaspermobile.domain;

import java.io.File;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class LoadFileRequest {
    private final String mResourceUri;
    private final File mTarget;

    public LoadFileRequest(String resourceUri, File target) {
        mResourceUri = resourceUri;
        mTarget = target;
    }

    public String getResourceUri() {
        return mResourceUri;
    }

    public File getTarget() {
        return mTarget;
    }
}
