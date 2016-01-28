package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.jaspersoft.android.sdk.client.oxm.control.validation.MandatoryValidationRule;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.network.entity.control.InputControlOption;
import com.jaspersoft.android.sdk.network.entity.control.InputControlState;
import com.jaspersoft.android.sdk.network.entity.control.ValidationRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        InputControl.class,
        InputControlState.class,
        ValidationRule.class,
        InputControlOption.class,
})
public class InputControlsMapperTest {

    private InputControlsMapper controlsMapper;

    @Mock
    InputControl mInputControl;
    @Mock
    InputControlState mInputControlState;
    @Mock
    ValidationRule mValidationRule;
    @Mock
    InputControlOption mInputControlOption;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMocks();
        controlsMapper = new InputControlsMapper();
    }

    @Test
    public void should_transform_mandatory_control() throws Exception {
        com.jaspersoft.android.sdk.client.oxm.control.InputControl legacyControl = controlsMapper.retrofittedControlToLegacy(mInputControl);
        assertThat("Should map control id", legacyControl.getId(), is("id of input control"));
        assertThat("Should map control label", legacyControl.getLabel(), is("label of input control"));
        assertThat("Should map control type", legacyControl.getType(), is(com.jaspersoft.android.sdk.client.oxm.control.InputControl.Type.singleValueText));
        assertThat("Should map control uri", legacyControl.getUri(), is("/my/uri"));
        assertThat("Should map control visibility flag", legacyControl.isVisible(), is(true));
        assertThat("Should map control mandatory flag", legacyControl.isMandatory(), is(true));
        assertThat("Should map control readonly flag", legacyControl.isReadOnly(), is(true));
        assertThat("Should map control master dependencies", legacyControl.getMasterDependencies(), hasItem("master1"));
        assertThat("Should map control slave dependencies", legacyControl.getSlaveDependencies(), hasItem("slave1"));

        com.jaspersoft.android.sdk.client.oxm.control.validation.ValidationRule legacyValidationRule = legacyControl.getValidationRules().get(0);
        assertThat("Should map validation rule error message", legacyValidationRule.getErrorMessage(), is("validation error message"));
        assertThat("Should map validation rule as mandatory type", legacyValidationRule, is(instanceOf(MandatoryValidationRule.class)));

        com.jaspersoft.android.sdk.client.oxm.control.InputControlState legacyState = legacyControl.getState();
        assertThat("Should map control state id", legacyState.getId(), is("id of state"));
        assertThat("Should map control state value", legacyState.getValue(), is("value of state"));
        assertThat("Should map control state uri", legacyState.getUri(), is("/my/uri"));

        com.jaspersoft.android.sdk.client.oxm.control.InputControlOption legacyOption = legacyState.getOptions().get(0);
        assertThat("Should map control option value", legacyOption.getValue(), is("value of option"));
        assertThat("Should map control option label", legacyOption.getLabel(), is("label of option"));
        assertThat("Should map control option selected flag", legacyOption.isSelected(), is(true));
    }

    @Test
    public void should_transform_datetime_control() throws Exception {
        when(mValidationRule.getType()).thenReturn("dateTimeFormatValidationRule");
        com.jaspersoft.android.sdk.client.oxm.control.InputControl legacyControl = controlsMapper.retrofittedControlToLegacy(mInputControl);
        com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule legacyValidationRule = (DateTimeFormatValidationRule) legacyControl.getValidationRules().get(0);
        assertThat("Should map validation rule error message", legacyValidationRule.getErrorMessage(), is("validation error message"));
        assertThat("Should map validation rule format", legacyValidationRule.getFormat(), is("value of validation rule"));
    }

    private void setUpMocks() {
        when(mInputControl.getId()).thenReturn("id of input control");
        when(mInputControl.getLabel()).thenReturn("label of input control");
        when(mInputControl.getType()).thenReturn("singleValueText");
        when(mInputControl.getUri()).thenReturn("/my/uri");
        when(mInputControl.isVisible()).thenReturn(true);
        when(mInputControl.isReadOnly()).thenReturn(true);
        when(mInputControl.isMandatory()).thenReturn(true);

        when(mInputControl.getMasterDependencies()).thenReturn(new HashSet<String>(Collections.singletonList("master1")));
        when(mInputControl.getSlaveDependencies()).thenReturn(new HashSet<String>(Collections.singletonList("slave1")));

        when(mInputControl.getValidationRules()).thenReturn(Collections.singleton(mValidationRule));
        when(mValidationRule.getValue()).thenReturn("value of validation rule");
        when(mValidationRule.getErrorMessage()).thenReturn("validation error message");
        when(mValidationRule.getType()).thenReturn("mandatoryValidationRule");

        when(mInputControl.getState()).thenReturn(mInputControlState);
        when(mInputControlState.getId()).thenReturn("id of state");
        when(mInputControlState.getUri()).thenReturn("/my/uri");
        when(mInputControlState.getValue()).thenReturn("value of state");
        when(mInputControlState.getOptions()).thenReturn(Collections.singleton(mInputControlOption));

        when(mInputControlOption.getValue()).thenReturn("value of option");
        when(mInputControlOption.getLabel()).thenReturn("label of option");
        when(mInputControlOption.isSelected()).thenReturn(true);
    }
}