package com.jaspersoft.android.jaspermobile.domain.network;

import com.jaspersoft.android.jaspermobile.data.network.ServerApiFactory;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.service.info.ServerInfoService;

/**
 * Abstracts out server info operations from SDK
 * <br/>
 * Implemented by {@link ServerApiFactory.ServerApiImpl }

 * @author Tom Koptel
 * @since 2.3
 */
public interface ServerApi {
    /**
     * Fills out metadata about server
     *
     * @return wrapper around server metadata
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    JasperServer requestServer() throws RestStatusException;

    interface Factory {
        /**
         * Creates different server api  on the basis of {@link ServerInfoService}
         *
         * @param infoService target server address
         * @return implementation of server api
         */
        ServerApi create(ServerInfoService infoService);
    }
}
