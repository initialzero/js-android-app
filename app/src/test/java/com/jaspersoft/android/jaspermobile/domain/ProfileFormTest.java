package com.jaspersoft.android.jaspermobile.domain;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileFormTest {
    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(ProfileForm.class).verify();
    }
}