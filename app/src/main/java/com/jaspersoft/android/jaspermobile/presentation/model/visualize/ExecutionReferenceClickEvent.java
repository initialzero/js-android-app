package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.visualize.ReportData;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ExecutionReferenceClickEvent {
    @NonNull
    private final ReportData mReportData;

    public ExecutionReferenceClickEvent(@NonNull ReportData reportData) {
        mReportData = reportData;
    }

    @NonNull
    public ReportData getReportData() {
        return mReportData;
    }
}
