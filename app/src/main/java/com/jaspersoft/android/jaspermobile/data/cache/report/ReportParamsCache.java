package com.jaspersoft.android.jaspermobile.data.cache.report;


import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportParamsCache {
    void put(String uri, List<ReportParameter> parameters);
    List<ReportParameter> get(String uri);
}
