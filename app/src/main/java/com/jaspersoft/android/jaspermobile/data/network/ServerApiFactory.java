package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ServerInfoDataMapper;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.sdk.service.data.server.ServerInfo;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
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
    private final RestStatusExceptionMapper mRestStatusExceptionMapper;

    @Inject
    public ServerApiFactory(ServerInfoDataMapper serverInfoDataMapper,
                            RestStatusExceptionMapper restStatusExceptionMapper) {
        mServerInfoDataMapper = serverInfoDataMapper;
        mRestStatusExceptionMapper = restStatusExceptionMapper;
    }

    @Override
    public ServerApi create(String baseUrl) {
        return new ServerApiImpl(baseUrl, mServerInfoDataMapper, mRestStatusExceptionMapper);
    }

    private static class ServerApiImpl implements ServerApi {
        private final String mBaseUrl;
        private final ServerInfoDataMapper mServerInfoDataMapper;
        private final RestStatusExceptionMapper mRestStatusExceptionMapper;

        private ServerApiImpl(String baseUrl,
                              ServerInfoDataMapper serverInfoDataMapper,
                              RestStatusExceptionMapper restStatusExceptionMapper) {
            mBaseUrl = baseUrl;
            mServerInfoDataMapper = serverInfoDataMapper;
            mRestStatusExceptionMapper = restStatusExceptionMapper;
        }

        @Override
        public JasperServer requestServer() throws RestStatusException {
            ServerInfoService service = ServerInfoService.create(mBaseUrl);
            try {
                ServerInfo serverInfo = service.requestServerInfo();
                return mServerInfoDataMapper.transform(mBaseUrl, serverInfo);
            } catch (ServiceException e) {
                throw mRestStatusExceptionMapper.transform(e);
            }
        }
    }
}
