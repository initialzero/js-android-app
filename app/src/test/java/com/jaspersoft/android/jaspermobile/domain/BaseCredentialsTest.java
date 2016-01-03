package com.jaspersoft.android.jaspermobile.domain;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class BaseCredentialsTest {
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(BaseCredentials.class).verify();
    }
}