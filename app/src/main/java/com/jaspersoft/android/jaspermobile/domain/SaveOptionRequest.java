package com.jaspersoft.android.jaspermobile.domain;

import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SaveOptionRequest {
    private final String mReportUri;
    private final String mLabel;
    private final List<ReportParameter> mParams;

    public SaveOptionRequest(String reportUri, String label, List<ReportParameter> params) {
        mReportUri = reportUri;
        mLabel = label;
        mParams = params;
    }

    public String getLabel() {
        return mLabel;
    }

    public List<ReportParameter> getParams() {
        return mParams;
    }

    public String getReportUri() {
        return mReportUri;
    }
}
