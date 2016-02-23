package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.SpringCredentials;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class CredentialsMapper {
    @Inject
    public CredentialsMapper() {
    }

    public Credentials toNetworkModel(AppCredentials appCredentials) {
        return SpringCredentials.builder()
                .withUsername(appCredentials.getUsername())
                .withPassword(appCredentials.getPassword())
                .withOrganization(appCredentials.getOrganization())
                .build();
    }
}
