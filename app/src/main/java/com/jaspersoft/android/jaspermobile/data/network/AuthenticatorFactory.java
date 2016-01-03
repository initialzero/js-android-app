package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.sdk.network.SpringCredentials;
import com.jaspersoft.android.sdk.service.auth.AuthorizationService;
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
    public Authenticator create(AuthorizationService authService) {
        return new AuthenticatorImpl(authService, mRestStatusExceptionMapper);
    }

    private static class AuthenticatorImpl implements Authenticator {
        private final AuthorizationService mAuthorizationService;
        private final RestStatusExceptionMapper mRestStatusExceptionMapper;

        private AuthenticatorImpl(AuthorizationService authorizationService, RestStatusExceptionMapper restStatusExceptionMapper) {
            mAuthorizationService = authorizationService;
            mRestStatusExceptionMapper = restStatusExceptionMapper;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void authenticate(BaseCredentials credentials) throws RestStatusException {
            SpringCredentials spring = SpringCredentials.builder()
                    .withUsername(credentials.getUsername())
                    .withPassword(credentials.getPassword())
                    .withOrganization(credentials.getOrganization())
                    .build();
            try {
                mAuthorizationService.authorize(spring);
            } catch (ServiceException e) {
                throw mRestStatusExceptionMapper.transform(e);
            }
        }
    }
}
