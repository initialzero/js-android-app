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
import com.jaspersoft.android.jaspermobile.data.FakeAccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TokenCacheImplTest {

    private TokenCacheImpl cacheUnderTest;
    private Profile fakeProfile = Profile.create("alias");

    @Before
    public void setUp() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        cacheUnderTest = new TokenCacheImpl(accountManager, FakeAccountDataMapper.get());
    }

    @Test
    public void testPut() throws Exception {
        Account fakeAccount = FakeAccount.injectAccount(fakeProfile).done();

        cacheUnderTest.put(fakeProfile, "token");

        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        String tokenFromAccountManager = accountManager.peekAuthToken(fakeAccount, "FULL ACCESS");
        assertThat("Failed to put token inside account manager",
                "token".equals(tokenFromAccountManager)
        );
    }

    @Test
    public void testGet() throws Exception {
        FakeAccount.injectAccount(fakeProfile)
                .injectToken("token")
                .done();
        assertThat("Failed to retrieve token from AccountManager",
                "token".equals(cacheUnderTest.get(fakeProfile))
        );
    }

    @Test
    public void testIsCached() throws Exception {
        FakeAccount.injectAccount(fakeProfile)
                .injectToken("token")
                .done();
        assertThat("Precondition failed token should be retained in AccountManager",
                cacheUnderTest.isCached(fakeProfile)
        );
    }
}