package com.jaspersoft.android.jaspermobile.domain;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class AppCredentialsTest {
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(AppCredentials.class).verify();
    }
}