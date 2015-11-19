package com.jaspersoft.android.jaspermobile.domain.network;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface Authenticator {
    String authenticate(BaseCredentials credentials) throws RestStatusException;

    interface Factory {
        Authenticator create(String baseUrl);
    }
}
