package com.jaspersoft.android.jaspermobile.domain;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportPage {
    public static final ReportPage EMPTY = new ReportPage(null, true);

    private final String mContent;
    private final boolean mFinal;

    public ReportPage(String content, boolean aFinal) {
        mContent = content;
        mFinal = aFinal;
    }

    public String getContent() {
        return mContent;
    }

    public boolean isFinal() {
        return mFinal;
    }

    @Override
    public String toString() {
        return "ReportPage{" +
                "mContent='" + mContent + '\'' +
                ", mFinal=" + mFinal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportPage that = (ReportPage) o;

        if (mFinal != that.mFinal) return false;
        return !(mContent != null ? !mContent.equals(that.mContent) : that.mContent != null);
    }

    @Override
    public int hashCode() {
        int result = mContent != null ? mContent.hashCode() : 0;
        result = 31 * result + (mFinal ? 1 : 0);
        return result;
    }
}

