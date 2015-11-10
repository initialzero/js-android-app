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

package com.jaspersoft.android.jaspermobile.domain.validator;

import com.jaspersoft.android.jaspermobile.data.BaseCredentials;
import com.jaspersoft.android.sdk.service.auth.JrsAuthenticator;
import com.jaspersoft.android.sdk.service.auth.SpringCredentials;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.verify;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JrsAuthenticator.class})
public class SpringCredentialsValidatorTest {
    @Mock
    JrsAuthenticator mJrsAuthenticator;
    SpringCredentialsValidator validator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        validator = new SpringCredentialsValidator(mJrsAuthenticator);
    }

    @Test
    public void testValidate() throws Exception {
        BaseCredentials appCredentials = BaseCredentials.builder()
                .setPassword("1234")
                .setUsername("username")
                .setOrganization("organization")
                .create();
        SpringCredentials credentials = SpringCredentials.builder()
                .password("1234")
                .username("username")
                .organization("organization")
                .build();

        validator.validate(appCredentials);
        verify(mJrsAuthenticator).authenticate(credentials);
    }
}