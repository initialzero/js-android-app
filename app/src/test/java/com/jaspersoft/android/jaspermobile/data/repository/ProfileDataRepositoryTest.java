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

import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.data.repository.profile.ProfileDataRepository;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ProfileDataRepositoryTest {

    @Mock
    ProfileCache mAccountCache;
    @Mock
    ActiveProfileCache mPreferencesCache;


    ProfileDataRepository repositoryUnderTest;
    Profile fakeProfile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        fakeProfile = Profile.create("any");
        repositoryUnderTest = new ProfileDataRepository(mAccountCache, mPreferencesCache);

        when(mAccountCache.put(any(Profile.class))).thenReturn(fakeProfile);
    }

    @Test
    public void should_save() throws Exception {
        TestSubscriber<Profile> test = new TestSubscriber<>();
        repositoryUnderTest.saveProfile(fakeProfile).subscribe(test);

        verify(mAccountCache).put(fakeProfile);
    }

    @Test
    public void should_delegate_activate_on_preference_cache() throws Exception {
        when(mPreferencesCache.put(any(Profile.class))).thenReturn(fakeProfile);

        TestSubscriber<Profile> test = new TestSubscriber<>();
        repositoryUnderTest.activate(fakeProfile).subscribe(test);

        verify(mPreferencesCache).put(fakeProfile);
        verifyNoMoreInteractions(mPreferencesCache);
    }
}