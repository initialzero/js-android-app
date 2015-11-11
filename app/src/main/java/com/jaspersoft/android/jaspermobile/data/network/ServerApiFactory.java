package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ServerInfoDataMapper;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.server.ServerInfoService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class ServerApiFactory implements ServerApi.Factory {

    private final ServerInfoDataMapper mServerInfoDataMapper;

    @Inject
    public ServerApiFactory(ServerInfoDataMapper serverInfoDataMapper) {
        mServerInfoDataMapper = serverInfoDataMapper;
    }

    @Override
    public ServerApi create(String baseUrl) {
        return new ServerApiImpl(baseUrl, mServerInfoDataMapper);
    }

    private static class ServerApiImpl implements ServerApi {
        private final ServerInfoDataMapper mServerInfoDataMapper;
        private final String mBaseUrl;

        private ServerApiImpl(String baseUrl, ServerInfoDataMapper serverInfoDataMapper) {
            mBaseUrl = baseUrl;
            mServerInfoDataMapper = serverInfoDataMapper;
        }

        @Override
        public JasperServer requestServer() {
            ServerInfoService service = ServerInfoService.create(mBaseUrl);
            ServerInfo serverInfo = service.requestServerInfo();
            return mServerInfoDataMapper.transform(mBaseUrl, serverInfo);
        }
    }
}
