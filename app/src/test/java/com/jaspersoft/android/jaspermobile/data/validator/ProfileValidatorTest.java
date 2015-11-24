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

import com.jaspersoft.android.jaspermobile.data.cache.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.DuplicateProfileException;
import com.jaspersoft.android.jaspermobile.domain.validator.exception.ProfileReservedException;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProfileValidatorTest {

    private ProfileValidatorImpl validator;

    @Mock
    ProfileCache profileCache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        validator = new ProfileValidatorImpl(profileCache);
    }

    @Test
    public void shouldRejectProfileIfAccountAlreadyRegistered() throws Exception {
        when(profileCache.hasProfile(any(Profile.class))).thenReturn(true);

        Profile fakeProfile = Profile.create("name");
        try {
            validator.validate(fakeProfile);
            fail("Account should not be valid");
        } catch (DuplicateProfileException ex) {
            assertThat(ex.requestedProfile(), is("name"));
        }
    }

   @Test
    public void shouldRejectProfileIfAccountNameIsReserved() throws Exception {
        try {
            validator.validate(Profile.create(JasperSettings.RESERVED_ACCOUNT_NAME));
            fail("Account should not be valid");
        } catch (ProfileReservedException ex) {
        }
    }

    @Test
    public void shouldAcceptProfileIfUnique() throws Exception {
        when(profileCache.hasProfile(any(Profile.class))).thenReturn(false);

        try {
            Profile fakeProfile = Profile.create("name");
            validator.validate(fakeProfile);
        } catch (DuplicateProfileException e) {
            fail("Account should be valid");
        }
    }
}