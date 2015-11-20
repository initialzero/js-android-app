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

import com.jaspersoft.android.jaspermobile.data.cache.ProfileActiveCache;
import com.jaspersoft.android.jaspermobile.data.cache.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.exception.FailedToSaveProfile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileDataRepositoryTest {

    @Mock
    ProfileCache mCache;
    @Mock
    ProfileActiveCache mActiveCache;

    ProfileDataRepository repositoryUnderTest;
    Profile fakeProfile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        fakeProfile = Profile.create("any");
        repositoryUnderTest = new ProfileDataRepository(mCache, mActiveCache);
    }

    @Test
    public void shouldNotSaveProfileIfInCache() throws Exception {
        when(mCache.hasProfile(any(Profile.class))).thenReturn(true);

        try {
            saveProfile();
            fail("Should not save profile. It is already in cache");
        } catch (FailedToSaveProfile ex) {
        }
    }

    @Test
    public void shouldSaveProfileForTheFirstTime() throws Exception {
        when(mCache.hasProfile(any(Profile.class))).thenReturn(false);
        when(mCache.put(any(Profile.class))).thenReturn(true);
        try {
            saveProfile();
        } catch (FailedToSaveProfile ex) {
            fail("Should save profile for the first time");
        }
    }

    @Test
    public void shouldNotSaveProfileIfCacheFailedToPerformOperation() {
        when(mCache.hasProfile(any(Profile.class))).thenReturn(false);
        when(mCache.put(any(Profile.class))).thenReturn(false);
        try {
            saveProfile();
            fail("If cache failed, should not save profile");
        } catch (FailedToSaveProfile ex) {
        }
    }

    @Test
    public void testActivate() throws Exception {
        repositoryUnderTest.activate(fakeProfile);
        verify(mActiveCache).put(fakeProfile);
        verifyNoMoreInteractions(mActiveCache);
    }

    //---------------------------------------------------------------------

    private void saveProfile() throws FailedToSaveProfile {
        repositoryUnderTest.saveProfile(fakeProfile);
    }
}