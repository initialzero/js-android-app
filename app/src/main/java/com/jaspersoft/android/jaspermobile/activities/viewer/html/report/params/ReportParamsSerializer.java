package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.params;

import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

public interface ReportParamsSerializer {
    String toJson(List<ReportParameter> controls);
}
