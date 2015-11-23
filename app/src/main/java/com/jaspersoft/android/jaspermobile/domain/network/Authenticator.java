package com.jaspersoft.android.jaspermobile.domain.network;

import com.jaspersoft.android.jaspermobile.data.network.AuthenticatorFactory;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;

/**
 * Abstracts out authentication operations from SDK
 * <br/>
 * Implemented by {@link AuthenticatorFactory.AuthenticatorImpl }
 *
 * @author Tom Koptel
 * @since 2.3
 */
public interface Authenticator {
    /**
     * Accepts app cached credentials and performs authentication call
     *
     * @param credentials used to authorize use
     * @return token or cookie from Jasper Server
     * @throws RestStatusException describes either network exception, http exception or Jasper Server specific error states
     */
    String authenticate(BaseCredentials credentials) throws RestStatusException;

    /**
     * Creates different authenticators on the basis of passed base url
     */
    interface Factory {
        /**
         * Creates different authenticators on the basis of passed base url
         *
         * @param baseUrl target server address
         * @return implementation of authenticator
         */
        Authenticator create(String baseUrl);
    }
}
