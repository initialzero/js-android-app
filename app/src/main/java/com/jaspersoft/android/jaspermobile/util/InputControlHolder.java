package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class InputControlHolder{
    private List<ReportParameter> mReportParams;
    private List<InputControl> mInputControls;
    private List<ReportOptionHolder> mReportOptions;

    public InputControlHolder() {
        this.mInputControls = new ArrayList<>();
        this.mReportOptions = new ArrayList<>();
        this.mReportParams = new ArrayList<>();
    }

    public List<InputControl> getInputControls() {
        return mInputControls;
    }

    public void setInputControls(List<InputControl> inputControls) {
        this.mInputControls = inputControls;
    }

    public List<ReportOptionHolder> getReportOptions() {
        return mReportOptions;
    }

    public void setReportOptions(List<ReportOptionHolder> reportOptions) {
        this.mReportOptions = reportOptions;
    }

    public List<ReportParameter> getReportParams() {
        return mReportParams;
    }

    public void setReportParams(List<ReportParameter> reportParams) {
        this.mReportParams = reportParams;
    }
}
