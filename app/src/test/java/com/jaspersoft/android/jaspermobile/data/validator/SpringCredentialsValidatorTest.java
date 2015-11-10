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
import com.jaspersoft.android.jaspermobile.domain.validator.exception.InvalidCredentialsException;
import com.jaspersoft.android.sdk.network.RestError;
import com.jaspersoft.android.sdk.service.auth.Credentials;
import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
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
    JrsAuthenticator mJrsAuthenticator;
    @Mock
    RestError mRestError;

    SpringCredentialsValidator validator;
    BaseCredentials credentialsUnderTest;

    @Rule
    public ExpectedException mException = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new SpringCredentialsValidator(mJrsAuthenticator);

        credentialsUnderTest = BaseCredentials.builder()
                .setPassword("1234")
                .setUsername("username")
                .setOrganization("organization")
                .create();
    }

    @Test
    public void testValidate() throws Exception {
        SpringCredentials credentials = SpringCredentials.builder()
                .password("1234")
                .username("username")
                .organization("organization")
                .build();

        validator.validate(credentialsUnderTest);
        verify(mJrsAuthenticator).authenticate(credentials);
    }

    @Test
    public void shouldThrowCheckedExceptionIfServerEncountered401() throws Exception {
        mException.expect(InvalidCredentialsException.class);
        mException.expectMessage("Client has passed either invalid password or username/organization combination");

        mockRestException(401);
        validator.validate(credentialsUnderTest);
    }

    @Test
    public void shouldReThrowHttpException() throws Exception {
        mException.expect(RestError.class);
        mockRestException(500);
        validator.validate(credentialsUnderTest);
    }

    private void mockRestException(int code) {
        when(mRestError.code()).thenReturn(code);
        when(mJrsAuthenticator.authenticate(any(Credentials.class))).thenThrow(mRestError);
    }
}