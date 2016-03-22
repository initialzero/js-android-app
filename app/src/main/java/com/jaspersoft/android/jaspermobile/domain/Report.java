package com.jaspersoft.android.jaspermobile.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class Report {

    @NonNull
    private final RxReportExecution mReportExecution;
    @NonNull
    private final String mReportUri;

    @Nullable
    private Integer mTotalPages;
    @Nullable
    private Boolean mMultiPage;

    public Report(@NonNull RxReportExecution reportExecution, @NonNull String reportUri) {
        mReportExecution = reportExecution;
        mReportUri = reportUri;
    }

    public RxReportExecution getExecution() {
        return mReportExecution;
    }

    public void setMultiPage(boolean multiPage) {
        mMultiPage = multiPage;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    @NonNull
    public String getReportUri() {
        return mReportUri;
    }

    @Nullable
    public Boolean getMultiPage() {
        return mMultiPage;
    }

    @Nullable
    public Integer getTotalPages() {
        return mTotalPages;
    }
}
