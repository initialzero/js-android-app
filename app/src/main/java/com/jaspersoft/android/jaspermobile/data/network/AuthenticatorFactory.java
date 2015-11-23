package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wraps delegates network calls to latest implementation of Jasper Android SDK
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class AuthenticatorFactory implements Authenticator.Factory {
    private final RestStatusExceptionMapper mRestStatusExceptionMapper;

    @Inject
    public AuthenticatorFactory(RestStatusExceptionMapper restStatusExceptionMapper) {
        mRestStatusExceptionMapper = restStatusExceptionMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticator create(String baseUrl) {
        return new AuthenticatorImpl(baseUrl, mRestStatusExceptionMapper);
    }

    private static class AuthenticatorImpl implements Authenticator {
        private final String mBaseUrl;
        private final RestStatusExceptionMapper mRestStatusExceptionMapper;

        private AuthenticatorImpl(String baseUrl, RestStatusExceptionMapper restStatusExceptionMapper) {
            mBaseUrl = baseUrl;
            mRestStatusExceptionMapper = restStatusExceptionMapper;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String authenticate(BaseCredentials credentials) throws RestStatusException {
            SpringCredentials spring = SpringCredentials.builder()
                    .username(credentials.getUsername())
                    .password(credentials.getPassword())
                    .build();
            try {
                return JrsAuthenticator.create(mBaseUrl).authenticate(spring);
            } catch (ServiceException e) {
                throw mRestStatusExceptionMapper.transform(e);
            }
        }
    }
}
