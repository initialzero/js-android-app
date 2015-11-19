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

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.CredentialsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.ProfileRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.TokenRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetTokenUseCaseTest {
    @Mock
    TokenRepository mTokenRepository;
    @Mock
    JasperServerRepository mServerRepository;
    @Mock
    CredentialsRepository mCredentialsRepository;

    @Mock
    BaseCredentials mCredentials;
    @Mock
    JasperServer mJasperServer;
    @Mock
    Profile mProfile;

    GetTokenUseCase useCase;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        useCase = new GetTokenUseCase(
                mTokenRepository,
                mServerRepository,
                mCredentialsRepository
        );

        when(mTokenRepository.getToken(any(JasperServer.class), any(BaseCredentials.class))).thenReturn("token");
        when(mServerRepository.getServer(any(Profile.class))).thenReturn(mJasperServer);
        when(mCredentialsRepository.getCredentials(any(Profile.class))).thenReturn(mCredentials);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTokenRetrievingOperation() throws Exception {
        assertThat(useCase.execute(mProfile), is("token"));

        verify(mServerRepository).getServer(mProfile);
        verify(mCredentialsRepository).getCredentials(mProfile);
        verify(mTokenRepository).getToken(mJasperServer, mCredentials);
    }
}
