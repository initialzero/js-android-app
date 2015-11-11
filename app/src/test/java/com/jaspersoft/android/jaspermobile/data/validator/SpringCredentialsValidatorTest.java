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

package com.jaspersoft.android.jaspermobile.data.validator;

import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.validator.Validation;
import com.jaspersoft.android.sdk.network.RestError;
import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JrsAuthenticator.class, RestError.class})
public class SpringCredentialsValidatorTest {
    @Mock
    Authenticator.Factory mAuthenticatorFactory;
    @Mock
    Authenticator mAuthenticator;
    @Mock
    RestError mRestError;

    SpringCredentialsValidator validator;
    BaseCredentials fakeCredentials;
    JasperServer fakeServer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        fakeServer = JasperServer.builder()
                .setBaseUrl("http://localhost")
                .setVersion(6.0d)
                .setEdition("CE")
                .create();
        fakeCredentials = BaseCredentials.builder()
                .setPassword("1234")
                .setUsername("username")
                .setOrganization("organization")
                .create();

        when(mAuthenticatorFactory.create(anyString())).thenReturn(mAuthenticator);
        validator = new SpringCredentialsValidator(mAuthenticatorFactory);
    }

    @Test
    public void testValidate() throws Exception {
        validator.create(fakeServer, fakeCredentials).perform();
        verify(mAuthenticatorFactory).create(fakeServer.getBaseUrl());
    }

    @Test
    public void shouldThrowCheckedExceptionIfServerEncountered401() throws Exception {
        mockRestException(401);

        Validation validation = validator.create(fakeServer, fakeCredentials);
        assertThat("Should be invalid as soon as API respond with 401", !validation.perform());
    }

    @Test(expected = RestError.class)
    public void shouldReThrowHttpException() throws Exception {
        mockRestException(500);
        validator.create(fakeServer, fakeCredentials).perform();
    }

    private void mockRestException(int code) {
        when(mRestError.code()).thenReturn(code);
        when(mAuthenticator.authenticate(any(BaseCredentials.class))).thenThrow(mRestError);
    }
}