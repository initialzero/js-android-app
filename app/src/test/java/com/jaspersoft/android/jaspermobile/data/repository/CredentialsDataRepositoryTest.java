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

package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.cache.CredentialsCache;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToRetrieveCredentials;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveCredentials;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsDataRepositoryTest {
    @Mock
    CredentialsCache mCredentialsCache;
    CredentialsDataRepository repoUnderTest;
    Profile fakeProfile;
    BaseCredentials fakeCredentials;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        repoUnderTest = new CredentialsDataRepository(mCredentialsCache);
        fakeProfile = Profile.create("name");
        fakeCredentials = BaseCredentials.builder()
                .setPassword("1234").setUsername("nay").create();
    }

    @Test
    public void testSaveCredentials() throws Exception {
        repoUnderTest.saveCredentials(fakeProfile, fakeCredentials);
        verify(mCredentialsCache).put(fakeProfile, fakeCredentials);
        verifyNoMoreInteractions(mCredentialsCache);
    }

    @Test
    public void testGetCredentials() throws Exception {
        repoUnderTest.getCredentials(fakeProfile);
        verify(mCredentialsCache).get(fakeProfile);
    }

    @Test
    public void testSaveCredentialsEncountersEncryptionError() throws Exception {
        doThrow(new PasswordManager.EncryptionException(null))
                .when(mCredentialsCache)
                .put(any(Profile.class), any(BaseCredentials.class));

        try {
            repoUnderTest.saveCredentials(fakeProfile, fakeCredentials);
            fail("Save credentials should rethrow FailedToSaveCredentials if password encryption operation failed");
        } catch (FailedToSaveCredentials ex) {
        }
    }

    @Test
    public void testGetCredentials1() throws Exception {
        when(mCredentialsCache.get(any(Profile.class))).thenThrow(new PasswordManager.DecryptionException(null));

        try {
            repoUnderTest.getCredentials(fakeProfile);
            fail("Get credentials should rethrow FailedToRetrieveCredentials if password decryption operation failed");
        } catch (FailedToRetrieveCredentials ex) {
        }
    }
}