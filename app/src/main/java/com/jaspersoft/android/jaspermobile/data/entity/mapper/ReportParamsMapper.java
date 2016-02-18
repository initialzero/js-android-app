package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardControlComponent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    public List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> legacyParamsToRetrofitted(List<ReportParameter> legacyParameters) {
        List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> list = new ArrayList<>(legacyParameters.size());
        for (ReportParameter legacyParameter : legacyParameters) {
            if (legacyParameter != null) {
                com.jaspersoft.android.sdk.network.entity.report.ReportParameter parameter = legacyParamToRetrofitted(legacyParameter);
                list.add(parameter);
            }
        }
        return list;
    }

    public com.jaspersoft.android.sdk.network.entity.report.ReportParameter legacyParamToRetrofitted(ReportParameter legacyParameter) {
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

    public List<ReportParameter> legacyControlsToParams(List<InputControl> inputControls) {
        List<ReportParameter> parameters = new ArrayList<>();
        for (InputControl inputControl : inputControls) {
            InputControlState state = inputControl.getState();
            ReportParameter reportParameter = mapStateToReportParameter(state);
            if (reportParameter != null) {
                parameters.add(reportParameter);
            }
        }
        return parameters;
    }

    @Nullable
    private ReportParameter mapStateToReportParameter(InputControlState state) {
        if (state == null) {
            return null;
        }

        ReportParameter reportParameter = new ReportParameter();
        Set<String> values = new HashSet<>();
        String value = state.getValue();
        if (value == null) {
            List<InputControlOption> options = state.getOptions();
            for (InputControlOption option : options) {
                if (option.isSelected()) {
                    values.add(option.getValue());
                }
            }
        } else {
            values.add(value);
        }
        reportParameter.setName(state.getId());
        reportParameter.setValues(values);
        return reportParameter;
    }

    public List<ReportParameter> adaptDashboardControlComponents(List<ReportParameter> reportParameters,
                                                                 List<DashboardControlComponent> components) {
        Map<String, Set<String>> map = legacyToMap(reportParameters);
        List<ReportParameter> parameters = new ArrayList<>(reportParameters.size());
        for (DashboardControlComponent component : components) {
            if (component != null) {
                Set<String> values = map.get(component.getControlId());
                if (values != null) {
                    ReportParameter reportParameter = new ReportParameter();
                    reportParameter.setName(component.getComponentId());
                    reportParameter.setValues(values);
                    parameters.add(reportParameter);
                }
            }
        }
        return parameters;
    }

    public List<ReportParameter> mapStatesToLegacyParams(List<InputControlState> states) {
        List<ReportParameter> parameters = new ArrayList<>(states.size());
        for (InputControlState state : states) {
            ReportParameter reportParameter = mapStateToReportParameter(state);
            if (reportParameter != null) {
                parameters.add(reportParameter);
            }
        }
        return parameters;
    }
}
