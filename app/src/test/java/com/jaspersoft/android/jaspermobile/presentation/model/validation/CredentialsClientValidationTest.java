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

import com.jaspersoft.android.jaspermobile.presentation.model.CredentialsModel;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.PasswordMissingException;
import com.jaspersoft.android.jaspermobile.presentation.model.validation.exception.UsernameMissingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsClientValidationTest {

    @Mock
    CredentialsModel mCredentialsModel;
    CredentialsClientValidation validation;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        validation = new CredentialsClientValidation();

        when(mCredentialsModel.getUsername()).thenReturn("username");
        when(mCredentialsModel.getPassword()).thenReturn("1234");
    }

    @Test(expected = UsernameMissingException.class)
    public void testThrowUsernameMissingExceptionIfNull() throws Exception {
        when(mCredentialsModel.getUsername()).thenReturn(null);
        validation.validate(mCredentialsModel);
    }

    @Test(expected = PasswordMissingException.class)
    public void testThrowPasswordMissingExceptionIfNull() throws Exception {
        when(mCredentialsModel.getPassword()).thenReturn(null);
        validation.validate(mCredentialsModel);
    }

    @Test(expected = UsernameMissingException.class)
    public void testThrowUsernameMissingExceptionIfEmptyString() throws Exception {
        when(mCredentialsModel.getUsername()).thenReturn("");
        validation.validate(mCredentialsModel);
    }

    @Test(expected = PasswordMissingException.class)
    public void testThrowPasswordMissingExceptionIfEmptyString() throws Exception {
        when(mCredentialsModel.getPassword()).thenReturn("");
        validation.validate(mCredentialsModel);
    }

    @Test(expected = UsernameMissingException.class)
    public void testThrowUsernameMissingExceptionIfSpacedString() throws Exception {
        when(mCredentialsModel.getUsername()).thenReturn(" ");
        validation.validate(mCredentialsModel);
    }

    @Test(expected = PasswordMissingException.class)
    public void testThrowPasswordMissingExceptionIfSpacedString() throws Exception {
        when(mCredentialsModel.getPassword()).thenReturn(" ");
        validation.validate(mCredentialsModel);
    }
}