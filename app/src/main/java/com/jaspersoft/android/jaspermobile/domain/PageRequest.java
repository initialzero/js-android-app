package com.jaspersoft.android.jaspermobile.domain;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class PageRequest {
    private final String mUri;
    private final String mRange;

    public PageRequest(String uri, String range) {
        mUri = uri;
        mRange = range;
    }

    public String getRange() {
        return mRange;
    }

    public String getUri() {
        return mUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageRequest that = (PageRequest) o;

        if (mUri != null ? !mUri.equals(that.mUri) : that.mUri != null) return false;
        return mRange != null ? mRange.equals(that.mRange) : that.mRange == null;

    }

    @Override
    public int hashCode() {
        int result = mUri != null ? mUri.hashCode() : 0;
        result = 31 * result + (mRange != null ? mRange.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "mRange='" + mRange + '\'' +
                ", mUri='" + mUri + '\'' +
                '}';
    }
}
