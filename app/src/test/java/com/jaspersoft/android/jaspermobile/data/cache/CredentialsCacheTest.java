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

package com.jaspersoft.android.jaspermobile.data.cache;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.jaspersoft.android.jaspermobile.data.FakeAccount;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.security.PasswordManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.crypto.BadPaddingException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialsCacheTest {
    private static final String ACCOUNT_TYPE = "com.jaspersoft";

    @Mock
    PasswordManager mPasswordManager;

    AccountManager accountManager;
    CredentialsCacheImpl cacheUnderTest;
    Profile fakeProfile;
    BaseCredentials fakeCredentials;
    FakeAccount fakeAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        accountManager = AccountManager.get(RuntimeEnvironment.application);
        cacheUnderTest = new CredentialsCacheImpl(RuntimeEnvironment.application, mPasswordManager, ACCOUNT_TYPE);
        fakeProfile = Profile.create("name");
        fakeCredentials = BaseCredentials.builder()
                .setPassword("1234")
                .setOrganization("organization")
                .setUsername("nay")
                .create();
        fakeAccount = new FakeAccount(RuntimeEnvironment.application, fakeProfile);
    }

    @Test
    public void testHappyPutCase() throws Exception {
        when(mPasswordManager.encrypt(anyString())).thenReturn("encrypted");
        fakeAccount.setup();

        assertThat("Put operation should save credentials if PasswordManager succeed",
                cacheUnderTest.put(fakeProfile, fakeCredentials)
        );

        Account account = fakeAccount.get();
        assertThat("Password should be injected in cache",
                accountManager.getPassword(account) != null);
        assertThat("Username should be injected in cache",
                fakeCredentials.getUsername().equals(accountManager.getUserData(account, "USERNAME_KEY"))
        );
        assertThat("Username should be injected in cache",
                fakeCredentials.getOrganization().equals(accountManager.getUserData(account, "ORGANIZATION_KEY"))
        );
        verify(mPasswordManager).encrypt(fakeCredentials.getPassword());
    }

    @Test
    public void putOperationShouldReturnFalseIfExceptionOccurred() throws Exception {
        when(mPasswordManager.encrypt(anyString())).thenThrow(
                new PasswordManager.EncryptionError(new BadPaddingException())
        );
        fakeAccount.setup();
        assertThat("Put operation should not save credentials if PasswordManager failed",
                !cacheUnderTest.put(fakeProfile, fakeCredentials)
        );
    }
}