/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.domain.interactor;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.repository.CredentialsDataRepository;
import com.jaspersoft.android.jaspermobile.data.repository.JasperServerDataRepository;
import com.jaspersoft.android.jaspermobile.data.repository.TokenDataRepository;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.TokenRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToRetrieveCredentials;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class GetTokenUseCase {
    /**
     * Injected by {@link ProfileModule#providesTokenRepository(TokenDataRepository)}
     */
    private final TokenRepository mTokenRepository;
    /**
     * Injected by {@link ProfileModule#providesServerRepository(JasperServerDataRepository)}
     */
    private final JasperServerRepository mServerRepository;
    /**
     * Injected by {@link ProfileModule#providesCredentialsRepository(CredentialsDataRepository)}
     */
    private final CredentialsRepository mCredentialsRepository;

    @Inject
    public GetTokenUseCase(TokenRepository tokenRepository,
                           JasperServerRepository serverRepository,
                           CredentialsRepository credentialsRepository) {
        mTokenRepository = tokenRepository;
        mServerRepository = serverRepository;
        mCredentialsRepository = credentialsRepository;
    }

    @NonNull
    public String execute(Profile profile) throws RestStatusException, FailedToRetrieveCredentials {
        JasperServer server = mServerRepository.getServer(profile);
        BaseCredentials credentials = mCredentialsRepository.getCredentials(profile);
        return mTokenRepository.getToken(profile, server, credentials);
    }
}
