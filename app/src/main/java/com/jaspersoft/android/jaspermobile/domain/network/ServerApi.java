package com.jaspersoft.android.jaspermobile.domain.network;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ServerApi {
    JasperServer requestServer();

    interface Factory {
        ServerApi create(String baseUrl);
    }
}
