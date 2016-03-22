package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportCompleteEvent {
    private final int mTotalPages;

    public ReportCompleteEvent(int totalPages) {
        mTotalPages = totalPages;
    }

    public int getTotalPages() {
        return mTotalPages;
    }
}
