/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
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
@PerProfile
public class InputControlsMapper {
    @Inject
    public InputControlsMapper() {
    }

    public List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> retrofittedControlsToLegacy(List<InputControl> controls) {
        List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> list = new ArrayList<>(controls.size());
        for (InputControl control : controls) {
            if (control != null) {
                com.jaspersoft.android.sdk.client.oxm.control.InputControl item = retrofittedControlToLegacy(control);
                list.add(item);
            }
        }
        return list;
    }

    public com.jaspersoft.android.sdk.client.oxm.control.InputControl retrofittedControlToLegacy(InputControl control) {
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
            com.jaspersoft.android.sdk.client.oxm.control.InputControlState legacyState = retrofittedStateToLegacy(state);
            legacyControl.setState(legacyState);
        }

        return legacyControl;
    }

    public List<com.jaspersoft.android.sdk.client.oxm.control.InputControlState> retrofittedStatesToLegacy(List<InputControlState> states) {
        List<com.jaspersoft.android.sdk.client.oxm.control.InputControlState> list = new ArrayList<>(states.size());
        for (InputControlState state : states) {
            if (state != null) {
                com.jaspersoft.android.sdk.client.oxm.control.InputControlState item = retrofittedStateToLegacy(state);
                list.add(item);
            }
        }
        return list;
    }

    public com.jaspersoft.android.sdk.client.oxm.control.InputControlState retrofittedStateToLegacy(InputControlState state) {
        com.jaspersoft.android.sdk.client.oxm.control.InputControlState legacyState = new com.jaspersoft.android.sdk.client.oxm.control.InputControlState();
        legacyState.setError(state.getError());
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
        return legacyState;
    }
}
