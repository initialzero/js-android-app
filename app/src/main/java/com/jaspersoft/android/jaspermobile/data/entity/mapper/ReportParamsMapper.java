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

    public String legacyParamsToJson(List<ReportParameter> reportParameters) {
        Map<String, Set<String>> params = legacyToMap(reportParameters);
        if (params.isEmpty()) {
            return EMPTY_JSON;
        }

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Set<String>>>() {
        }.getType();
        return gson.toJson(params, mapType);
    }

    public Map<String, Set<String>> legacyToMap(List<ReportParameter> parameters) {
        if (parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> params = new HashMap<String, Set<String>>(parameters.size());
        for (ReportParameter parameter : parameters) {
            params.put(parameter.getName(), parameter.getValues());
        }
        return params;
    }

    public List<ReportParameter> mapToLegacyParams(Map<String, Set<String>> params) {
        if (params != null && params.isEmpty() || params == null) {
            return Collections.emptyList();
        }
        List<ReportParameter> parameters = new ArrayList<>(params.size());
        for (Map.Entry<String, Set<String>> entry : params.entrySet()) {
            parameters.add(new ReportParameter(entry.getKey(), entry.getValue()));
        }
        return parameters;
    }
}
