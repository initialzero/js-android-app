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

package com.jaspersoft.android.jaspermobile.data.cache;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.jaspersoft.android.jaspermobile.data.FakeAccount;
import com.jaspersoft.android.jaspermobile.data.FakeAccountDataMapper;
import com.jaspersoft.android.jaspermobile.data.cache.profile.AccountCredentialsCache;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.account.AccountStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *     // TODO fix password issues
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountCredentialsCacheTest {
    @Mock
    SecureCache mSecureCache;

    AccountCredentialsCache cacheUnderTest;
    Profile fakeProfile;
    AppCredentials fakeCredentials;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mSecureCache.get(any(Profile.class), anyString())).thenReturn("encrypted");

        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        cacheUnderTest = new AccountCredentialsCache(accountManager, mSecureCache, FakeAccountDataMapper.get());
        fakeProfile = Profile.create("name");
        fakeCredentials = AppCredentials.builder()
                .setPassword("1234")
                .setOrganization("organization")
                .setUsername("nay")
                .create();
    }


    @Test
    public void testHappyPutCase() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        Account fakeAccount = FakeAccount.injectAccount(fakeProfile).done();

        cacheUnderTest.put(fakeProfile, fakeCredentials);
        verify(mSecureCache).put(fakeProfile, AccountStorage.KEY, "1234");

        assertThat("Username should be injected in cache",
                "nay".equals(accountManager.getUserData(fakeAccount, "USERNAME_KEY"))
        );
        assertThat("Organization should be injected in cache",
                "organization".equals(accountManager.getUserData(fakeAccount, "ORGANIZATION_KEY"))
        );
    }

    @Test
    public void testHappyGetCase() throws Exception {
        when(mSecureCache.get(any(Profile.class), anyString())).thenReturn("1234");

        FakeAccount.injectAccount(fakeProfile)
                .injectCredentials(fakeCredentials)
                .done();

        AppCredentials credentials = cacheUnderTest.get(fakeProfile);

        assertThat("Failed to retrieve password for profile " + fakeProfile,
                "1234".equals(credentials.getPassword()));
        assertThat("Failed to retrieve username for profile " + fakeProfile,
                "nay".equals(credentials.getUsername()));
        assertThat("Failed to retrieve organization for profile " + fakeProfile,
                "organization".equals(credentials.getOrganization()));
    }
}