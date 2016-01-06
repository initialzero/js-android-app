package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportParamsMapper {

    @Inject
    public ReportParamsMapper() {
    }

    public List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> transform(List<ReportParameter> legacyParameters) {
        List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> list = new ArrayList<>(legacyParameters.size());
        for (ReportParameter legacyParameter : legacyParameters) {
            if (legacyParameter != null) {
                com.jaspersoft.android.sdk.network.entity.report.ReportParameter parameter = transform(legacyParameter);
                list.add(parameter);
            }
        }
        return list;
    }

    public com.jaspersoft.android.sdk.network.entity.report.ReportParameter transform(ReportParameter legacyParameter) {
        return new com.jaspersoft.android.sdk.network.entity.report.ReportParameter(legacyParameter.getName(), legacyParameter.getValues());
    }
}
