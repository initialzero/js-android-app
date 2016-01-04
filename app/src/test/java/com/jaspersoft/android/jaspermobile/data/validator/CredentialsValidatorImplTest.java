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

import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.network.Authenticator;
import com.jaspersoft.android.jaspermobile.domain.network.RestErrorCodes;
import com.jaspersoft.android.jaspermobile.domain.network.RestStatusException;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.sdk.service.auth.AuthorizationService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsValidatorImplTest {
    @Mock
    RestStatusException mRestError;
    @Mock
    Authenticator.Factory mFactory;
    @Mock
    Authenticator mAuthenticator;
    @Mock
    AppCredentials credentialsUnderTest;
    @Mock
    AuthorizationService mAuthorizationService;
    @Mock
    JasperServer fakeServer;

    CredentialsValidator validator;

    @Rule
    public ExpectedException mException = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mFactory.create(mAuthorizationService)).thenReturn(mAuthenticator);
        validator = new CredentialsValidatorImpl(mFactory);
    }

    @Test
    public void testValidate() throws Exception {
        performValidation();
        verify(mAuthenticator).authenticate(credentialsUnderTest);
    }

    @Test
    public void shouldReThrowHttpException() throws Exception {
        mException.expect(RestStatusException.class);
        mockRestException(RestErrorCodes.INTERNAL_ERROR);

        TestSubscriber<Void> test = new TestSubscriber<>();
        validator.validate(credentialsUnderTest).subscribe(test);
    }

    private void performValidation() throws Exception {
    }

    private void mockRestException(int code) throws Exception {
        when(mRestError.code()).thenReturn(code);
        doThrow(mRestError).when(mAuthenticator).authenticate(any(AppCredentials.class));
    }
}