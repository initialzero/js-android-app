/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.CredentialsMapper;
import com.jaspersoft.android.jaspermobile.domain.ProfileForm;
import com.jaspersoft.android.jaspermobile.domain.validator.ValidationRule;
import com.jaspersoft.android.sdk.network.AnonymousClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.service.auth.AuthorizationService;

import java.net.CookieHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
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
