package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class JasperServerMapper {

    @Inject
    public JasperServerMapper() {
    }

    public JasperServer toDomainModel(String serverUrl, ServerInfo serverInfo) {
        return JasperServer.builder()
                .setBaseUrl(serverUrl)
                .setEditionIsPro(serverInfo.isEditionPro())
                .setVersion(serverInfo.getVersion())
                .create();
    }
}
