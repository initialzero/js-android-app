package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.fail;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileReservedValidationTest {
    private ProfileReservedValidation validator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        validator = new ProfileReservedValidation("reserved");
    }

    @Test
    public void should_fail_if_profile_added_with_reserved_name() throws Exception {
        Profile reservedProfile = Profile.create("reserved");
        try {
            validator.validate(reservedProfile);
            fail("Should fail with ProfileReservedException");
        } catch (Exception ex) {
            // stub
        }
    }
}
