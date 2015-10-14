package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.oxm.report.option.ReportOption;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ReportOptionHolder {
    private ReportOption mReportOption;
    private Integer mHashCode;
    private boolean mSelected;

    public ReportOptionHolder(ReportOption mReportOption, Integer mHashCode) {
        this.mHashCode = mHashCode;
        this.mReportOption = mReportOption;
    }

    public Integer getHashCode() {
        return mHashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.mHashCode = hashCode;
    }

    public ReportOption getReportOption() {
        return mReportOption;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }
}
