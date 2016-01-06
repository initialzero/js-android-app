package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.jaspermobile.internal.di.PerReport;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.jaspersoft.android.sdk.client.oxm.control.validation.MandatoryValidationRule;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.network.entity.control.InputControlOption;
import com.jaspersoft.android.sdk.network.entity.control.InputControlState;
import com.jaspersoft.android.sdk.network.entity.control.ValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerReport
public class InputControlsMapper {
    @Inject
    public InputControlsMapper() {
    }

    public List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> transform(List<InputControl> controls) {
        List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> list = new ArrayList<>(controls.size());
        for (InputControl control : controls) {
            if (control != null) {
                com.jaspersoft.android.sdk.client.oxm.control.InputControl item = transform(control);
                list.add(item);
            }
        }
        return list;
    }

    public com.jaspersoft.android.sdk.client.oxm.control.InputControl transform(InputControl control) {
        com.jaspersoft.android.sdk.client.oxm.control.InputControl legacyControl = new com.jaspersoft.android.sdk.client.oxm.control.InputControl();
        legacyControl.setId(control.getId());
        legacyControl.setLabel(control.getLabel());
        legacyControl.setType(com.jaspersoft.android.sdk.client.oxm.control.InputControl.Type.valueOf(control.getType()));
        legacyControl.setUri(control.getUri());
        legacyControl.setMandatory(control.isMandatory());
        legacyControl.setReadOnly(control.isReadOnly());
        legacyControl.setVisible(control.isVisible());
        legacyControl.setMasterDependencies(new ArrayList<String>(control.getMasterDependencies()));
        legacyControl.setSlaveDependencies(new ArrayList<String>(control.getSlaveDependencies()));

        Set<ValidationRule> validationRules = control.getValidationRules();
        List<com.jaspersoft.android.sdk.client.oxm.control.validation.ValidationRule> rules = new ArrayList<>(validationRules.size());
        for (ValidationRule validationRule : validationRules) {
            com.jaspersoft.android.sdk.client.oxm.control.validation.ValidationRule mappedRule;
            if (validationRule != null) {
                if ("dateTimeFormatValidationRule".equals(validationRule.getType())) {
                    DateTimeFormatValidationRule dateTimeFormatValidationRule = new DateTimeFormatValidationRule();
                    dateTimeFormatValidationRule.setFormat(validationRule.getValue());
                    mappedRule = dateTimeFormatValidationRule;
                } else {
                    mappedRule = new MandatoryValidationRule();
                }
                mappedRule.setErrorMessage(validationRule.getErrorMessage());
                rules.add(mappedRule);
            }
        }
        legacyControl.setValidationRules(rules);

        InputControlState state = control.getState();
        if (state != null) {
            com.jaspersoft.android.sdk.client.oxm.control.InputControlState legacyState = new com.jaspersoft.android.sdk.client.oxm.control.InputControlState();
            legacyState.setId(state.getId());
            legacyState.setUri(state.getUri());
            legacyState.setValue(state.getValue());

            Set<InputControlOption> options = state.getOptions();
            List<com.jaspersoft.android.sdk.client.oxm.control.InputControlOption> legacyOptions = new ArrayList<>(options.size());
            for (InputControlOption option : options) {
                if (option != null) {
                    com.jaspersoft.android.sdk.client.oxm.control.InputControlOption legacyOption = new com.jaspersoft.android.sdk.client.oxm.control.InputControlOption();
                    legacyOption.setValue(option.getValue());
                    legacyOption.setLabel(option.getLabel());
                    legacyOption.setSelected(option.isSelected());
                    legacyOptions.add(legacyOption);
                }
            }
            legacyState.setOptions(legacyOptions);
            legacyControl.setState(legacyState);
        }

        return legacyControl;
    }
}
