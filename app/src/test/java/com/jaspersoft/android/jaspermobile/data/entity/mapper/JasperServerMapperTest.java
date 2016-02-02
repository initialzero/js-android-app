package com.jaspersoft.android.jaspermobile.data.entity.mapper;


import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class JasperServerMapperTest {

    private static final String SERVER_URL = "http://localhost";
    private JasperServerMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new JasperServerMapper();
    }

    @Test
    public void testToDomainModel() throws Exception {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setVersion(ServerVersion.v5_5);
        serverInfo.setEdition("PRO");

        JasperServer result = mapper.toDomainModel(SERVER_URL, serverInfo);
        assertThat("Failed to map server url", result.getBaseUrl(), is(SERVER_URL));
        assertThat("Failed to map server version", result.getVersion(), is("5.5"));
        assertThat("Failed to map server edition", result.isProEdition(), is(true));
    }
}