package com.jaspersoft.android.jaspermobile.data;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class SpringCredentialsTest {
    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(SpringCredentials.class).verify();
    }
}