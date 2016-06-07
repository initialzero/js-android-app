/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.repository;

import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.repository.profile.CredentialsDataRepository;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
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

    private CredentialsDataRepository repoUnderTest;
    private AppCredentials fakeCredentials;
    private Profile fakeProfile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        repoUnderTest = new CredentialsDataRepository(mCredentialsCache);
        fakeProfile = Profile.create("name");
        fakeCredentials = AppCredentials.builder()
                .setPassword("1234")
                .setUsername("nay")
                .create();

        when(mCredentialsCache.put(any(Profile.class), any(AppCredentials.class))).thenReturn(fakeCredentials);
        when(mCredentialsCache.get(any(Profile.class))).thenReturn(fakeCredentials);
    }

    @Test
    public void should_validate_and_save_credentials() throws Exception {
        repoUnderTest.saveCredentials(fakeProfile, fakeCredentials);

        verify(mCredentialsCache).put(fakeProfile, fakeCredentials);
        verifyNoMoreInteractions(mCredentialsCache);
    }

    @Test
    public void should_retrieve_credentials_from_cache() throws Exception {
        repoUnderTest.getCredentials(fakeProfile);
        verify(mCredentialsCache).get(fakeProfile);
    }
}