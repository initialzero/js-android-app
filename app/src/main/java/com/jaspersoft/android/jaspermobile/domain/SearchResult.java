package com.jaspersoft.android.jaspermobile.domain;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class SearchResult {
    private final List<ResourceLookup> mLookups;
    private final boolean mReachedEnd;

    public SearchResult(List<ResourceLookup> lookups, boolean reachedEnd) {
        mLookups = lookups;
        mReachedEnd = reachedEnd;
    }

    public List<ResourceLookup> getLookups() {
        return mLookups;
    }

    public boolean isReachedEnd() {
        return mReachedEnd;
    }
}
