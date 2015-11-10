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
public class ProfileCacheImplTest {
    private static final String ACCOUNT_TYPE = "com.jaspersoft";

    ProfileCacheImpl cacheUnderTest;
    Profile fakeProfile;

    @Before
    public void setUp() throws Exception {
        cacheUnderTest = new ProfileCacheImpl(RuntimeEnvironment.application, ACCOUNT_TYPE);
        fakeProfile = Profile.create("name");
    }

    @Test
    public void testPut() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        assertThat("Failed precondition. There should be no accounts",
                accountManager.getAccountsByType(ACCOUNT_TYPE).length == 0
        );
        assertThat("Failed to put profile in cache",
                cacheUnderTest.put(fakeProfile)
        );
    }

    @Test
    public void testHasProfile() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        assertThat("Failed precondition. Account was not add",
                accountManager.addAccountExplicitly(new Account("name", ACCOUNT_TYPE), null, null)
        );
        assertThat("Failed precondition. Account was add but missing",
                accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0
        );
        assertThat("Cache should contain profile with key: 'name'",
                cacheUnderTest.hasProfile(fakeProfile)
        );
    }
}