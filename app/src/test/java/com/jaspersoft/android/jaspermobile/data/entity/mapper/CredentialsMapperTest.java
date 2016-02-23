package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.sdk.network.SpringCredentials;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsMapperTest {

    private CredentialsMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new CredentialsMapper();
    }

    @Test
    public void testToNetworkModel() throws Exception {
        AppCredentials credentials = AppCredentials.builder()
                .setOrganization("organization")
                .setUsername("user")
                .setPassword("1234")
                .create();

        SpringCredentials result = (SpringCredentials) mapper.toNetworkModel(credentials);
        assertThat("Failed to map organization", result.getOrganization(), is("organization"));
        assertThat("Failed to map username",result.getUsername(), is("user"));
        assertThat("Failed to map password",result.getPassword(), is("1234"));
    }
}