package com.jaspersoft.android.jaspermobile.domain;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class LoadOptionParamsRequest {
    private final String mOptionUri;
    private final String mReportUri;

    public LoadOptionParamsRequest(String optionUri, String reportUri) {
        mOptionUri = optionUri;
        mReportUri = reportUri;
    }

    public String getOptionUri() {
        return mOptionUri;
    }

    public String getReportUri() {
        return mReportUri;
    }
}
