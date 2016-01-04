package com.jaspersoft.android.jaspermobile.domain;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileFormTest {
    @Test
    public void testBaseUrlTrimmedDuringBuildProcess() {
        ProfileForm form = ProfileForm.builder()
                .setBaseUrl("http://localhost/")
                .build();
        assertThat(form.getServerUrl(), is("http://localhost"));
    }

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(ProfileForm.class).verify();
    }
}