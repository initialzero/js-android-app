package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.CredentialsMapper;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.sdk.network.AnonymousClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.service.auth.AuthorizationService;

import java.net.CookieHandler;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public final class ProfileAuthorizedValidation implements ValidationRule<ProfileForm, Exception> {
    private final Server.Builder mServerBuilder;
    private final CredentialsMapper mCredentialsMapper;

    @Inject
    public ProfileAuthorizedValidation(Server.Builder serverBuilder,
                                       CredentialsMapper credentialsMapper) {
        mServerBuilder = serverBuilder;
        mCredentialsMapper = credentialsMapper;
    }

    @Override
    public void validate(ProfileForm form) throws Exception {
        Server server = mServerBuilder.withBaseUrl(form.getServerUrl()).build();
        AnonymousClient client = server.newClient().withCookieHandler(CookieHandler.getDefault()).create();
        AuthorizationService authorizationService = AuthorizationService.newService(client);

        Credentials credentials = mCredentialsMapper.toNetworkModel(form.getCredentials());
        authorizationService.authorize(credentials);
    }
}
