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

import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.repository.profile.CredentialsDataRepository;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.validator.CredentialsValidator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * TODO fix password issues
 * @author Tom Koptel
 * @since 2.3
 */
public class CredentialsDataRepositoryTest {
    @Mock
    CredentialsCache mCredentialsCache;
    @Mock
    CredentialsValidator mCredentialsValidator;

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

        when(mCredentialsValidator.validate(any(AppCredentials.class))).thenReturn(Observable.just(fakeCredentials));
        when(mCredentialsCache.putAsObservable(any(Profile.class), any(AppCredentials.class))).thenReturn(Observable.just(fakeCredentials));
        when(mCredentialsCache.getAsObservable(any(Profile.class))).thenReturn(Observable.just(fakeCredentials));
    }

    @Test
    public void should_validate_and_save_credentials() throws Exception {
        TestSubscriber<Profile> test = new TestSubscriber<>();
        repoUnderTest.saveCredentials(fakeProfile, fakeCredentials).subscribe(test);
        test.assertNoErrors();

        verify(mCredentialsCache).putAsObservable(fakeProfile, fakeCredentials);
        verifyNoMoreInteractions(mCredentialsCache);
    }

    @Test
    public void should_retrieve_credentials_from_cache() throws Exception {
        TestSubscriber<AppCredentials> test = new TestSubscriber<>();
        repoUnderTest.getCredentials(fakeProfile).subscribe(test);
        test.assertNoErrors();

        verify(mCredentialsCache).getAsObservable(fakeProfile);
    }

//    @Test
//    public void testSaveCredentialsEncountersEncryptionError() throws Exception {
//        when(mCredentialsCache.putAsObservable(any(Profile.class), any(AppCredentials.class)))
//                .thenReturn(Observable.<AppCredentials>error(new PasswordManager.EncryptionException(null)));
//
//        TestSubscriber<Profile> test = new TestSubscriber<>();
//        repoUnderTest.saveCredentials(fakeProfile, fakeCredentials).subscribe(test);
//
//        FailedToSaveCredentials ex = (FailedToSaveCredentials) test.getOnErrorEvents().get(0);
//        assertThat("Save credentials should rethrow FailedToSaveCredentials if password encryption operation failed", ex, is(notNullValue()));
//    }
//
//    @Test
//    public void should_contain_error_if_decryption_failed() throws Exception {
//        when(mCredentialsCache.getAsObservable(any(Profile.class)))
//                .thenReturn(Observable.<AppCredentials>error(new PasswordManager.DecryptionException(null)));
//
//        TestSubscriber<AppCredentials> test = new TestSubscriber<>();
//        repoUnderTest.getCredentials(fakeProfile).subscribe(test);
//
//        FailedToRetrieveCredentials ex = (FailedToRetrieveCredentials) test.getOnErrorEvents().get(0);
//        assertThat("Get credentials should rethrow FailedToRetrieveCredentials if password decryption operation failed", ex, is(notNullValue()));
//    }
}