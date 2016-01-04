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

import com.jaspersoft.android.jaspermobile.data.entity.mapper.CredentialsMapper;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.service.rx.auth.RxAuthorizationService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsValidatorImplTest {

    @Mock
    Credentials mCredentials;
    @Mock
    RxAuthorizationService mService;

    @Mock
    CredentialsMapper mCredentialsMapper;

    CredentialsValidator validator;

    @Rule
    public ExpectedException mException = ExpectedException.none();
    private AppCredentials fakeCredentials;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        fakeCredentials = AppCredentials.builder()
                .setOrganization("organziation")
                .setPassword("password")
                .setUsername("username")
                .create();
        validator = new CredentialsValidatorImpl(mService, mCredentialsMapper);
    }

    @Test
    public void testValidate() throws Exception {
        when(mService.authorize(any(Credentials.class))).thenReturn(Observable.just(mCredentials));
        when(mCredentialsMapper.toNetworkModel(any(AppCredentials.class))).thenReturn(mCredentials);

        TestSubscriber<AppCredentials> test = new TestSubscriber<>();
        validator.validate(fakeCredentials).subscribe(test);
        test.assertNoErrors();

        verify(mService).authorize(mCredentials);
    }
}