package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ServerInfoDataMapper;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.network.ServerApi;
import com.jaspersoft.android.sdk.service.info.ServerInfoService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wraps delegates network calls to latest implementation of Jasper Android SDK
 *
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerApi create(ServerInfoService infoService) {
        return new ServerApiImpl(infoService, mServerInfoDataMapper, mRestStatusExceptionMapper);
    }

    private static class ServerApiImpl implements ServerApi {
        private final ServerInfoService mServerInfoService;
        private final ServerInfoDataMapper mServerInfoDataMapper;
        private final RestStatusExceptionMapper mRestStatusExceptionMapper;

        private ServerApiImpl(ServerInfoService serverInfoService, ServerInfoDataMapper serverInfoDataMapper,
                              RestStatusExceptionMapper restStatusExceptionMapper) {
            mServerInfoService = serverInfoService;
            mServerInfoDataMapper = serverInfoDataMapper;
            mRestStatusExceptionMapper = restStatusExceptionMapper;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JasperServer requestServer() throws RestStatusException {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
