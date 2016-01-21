package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportParamsMapper {
    private static final String EMPTY_JSON = "{}";

    @Inject
    public ReportParamsMapper() {
    }

    public List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> toRetrofittedParams(List<ReportParameter> legacyParameters) {
        List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> list = new ArrayList<>(legacyParameters.size());
        for (ReportParameter legacyParameter : legacyParameters) {
            if (legacyParameter != null) {
                com.jaspersoft.android.sdk.network.entity.report.ReportParameter parameter = toRetrofittedParam(legacyParameter);
                list.add(parameter);
            }
        }
        return list;
    }

    public com.jaspersoft.android.sdk.network.entity.report.ReportParameter toRetrofittedParam(ReportParameter legacyParameter) {
        return new com.jaspersoft.android.sdk.network.entity.report.ReportParameter(legacyParameter.getName(), legacyParameter.getValues());
    }

    public String toJsonLegacyParams(List<ReportParameter> reportParameters) {
        Map<String, Set<String>> params = toMapLegacy(reportParameters);
        if (params.isEmpty()) {
            return EMPTY_JSON;
        }

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Set<String>>>() {
        }.getType();
        return gson.toJson(params, mapType);
    }

    public Map<String, Set<String>> toMapLegacy(List<ReportParameter> parameters) {
        if (parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> params = new HashMap<String, Set<String>>(parameters.size());
        for (ReportParameter parameter : parameters) {
            params.put(parameter.getName(), parameter.getValues());
        }
        return params;
    }
}
