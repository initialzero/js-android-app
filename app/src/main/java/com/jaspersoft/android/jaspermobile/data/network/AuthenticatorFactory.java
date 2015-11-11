package com.jaspersoft.android.jaspermobile.data.network;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class AuthenticatorFactory implements Authenticator.Factory {

    @Inject
    public AuthenticatorFactory() {
    }

    @Override
    public Authenticator create(String baseUrl) {
        return new AuthenticatorImpl(baseUrl);
    }

    private static class AuthenticatorImpl implements Authenticator {
        private final String mBaseUrl;

        private AuthenticatorImpl(String baseUrl) {
            mBaseUrl = baseUrl;
        }

        @Override
        public String authenticate(BaseCredentials credentials) {
            SpringCredentials spring = SpringCredentials.builder()
                    .username(credentials.getUsername())
                    .password(credentials.getPassword())
                    .build();
            return JrsAuthenticator.create(mBaseUrl).authenticate(spring);
        }
    }
}
