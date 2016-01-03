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

package com.jaspersoft.android.jaspermobile.presentation.model.validation;

import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.AliasMissingException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.ServerUrlFormatException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.ServerUrlMissingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileClientValidationTest {

    @Mock
    ProfileModel mProfileModel;

    ProfileClientValidation validation;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        validation = new ProfileClientValidation();

        when(mProfileModel.getServerUrl()).thenReturn("http://localhost");
        when(mProfileModel.getAlias()).thenReturn("alias");
    }

    @Test(expected = AliasMissingException.class)
    public void testThrowAliasMissingExceptionIfNull() throws Exception {
        when(mProfileModel.getAlias()).thenReturn(null);
        validation.validate(mProfileModel);
    }

    @Test(expected = AliasMissingException.class)
    public void testThrowAliasMissingExceptionIfEmptyString() throws Exception {
        when(mProfileModel.getAlias()).thenReturn("");
        validation.validate(mProfileModel);
    }

    @Test(expected = AliasMissingException.class)
    public void testThrowAliasMissingExceptionIfSpacedString() throws Exception {
        when(mProfileModel.getAlias()).thenReturn(" ");
        validation.validate(mProfileModel);
    }

    @Test(expected = ServerUrlMissingException.class)
    public void testThrowServerUrlMissingExceptionIfNull() throws Exception {
        when(mProfileModel.getServerUrl()).thenReturn(null);
        validation.validate(mProfileModel);
    }

    @Test(expected = ServerUrlMissingException.class)
    public void testThrowServerUrlMissingExceptionIfEmptyString() throws Exception {
        when(mProfileModel.getServerUrl()).thenReturn("");
        validation.validate(mProfileModel);
    }

    @Test(expected = ServerUrlMissingException.class)
    public void testThrowServerUrlMissingExceptionIfSpacedString() throws Exception {
        when(mProfileModel.getServerUrl()).thenReturn(" ");
        validation.validate(mProfileModel);
    }

    @Test(expected = ServerUrlFormatException.class)
    public void testThrowServerUrlFormatExceptionIfUrlInvalid() throws Exception {
        when(mProfileModel.getServerUrl()).thenReturn("localhost.com");
        validation.validate(mProfileModel);
    }
}