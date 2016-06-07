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
import com.jaspersoft.android.jaspermobile.data.cache.profile.AccountProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountProfileCacheTest {
    AccountProfileCache cacheUnderTest;
    Profile fakeProfile;

    @Rule
    public ExpectedException mException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        cacheUnderTest = new AccountProfileCache(accountManager, FakeAccountDataMapper.get());
        fakeProfile = Profile.create("name");
    }

    @Test
    public void testPut() throws Exception {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        assertThat("Failed precondition. There should be no accounts",
                accountManager.getAccountsByType(FakeAccount.TYPE).length == 0
        );

        cacheUnderTest.put(fakeProfile);

        Account account = new Account("name", JasperSettings.JASPER_ACCOUNT_TYPE);
        String alias = accountManager.getUserData(account, "ALIAS_KEY");
        assertThat("Failed to put profile alias in cache",
                alias != null
        );
    }

    @Test
    public void testHasProfile() throws Exception {
        givenRegisteredAccount("name");
        assertThat("Cache should contain profile with key: 'name'",
                cacheUnderTest.hasProfile(fakeProfile)
        );
    }

    @Test
    public void testListAllProfiles() throws Exception {
        givenRegisteredAccount("name1");
        givenRegisteredAccount("name2");

        List<Profile> profiles = cacheUnderTest.getAll();
        assertThat(profiles, hasItems(Profile.create("name1"), Profile.create("name2")));
    }

    private void givenRegisteredAccount(String name) {
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        assertThat("Failed precondition. Account was not add",
                accountManager.addAccountExplicitly(new Account(name, FakeAccount.TYPE), null, null)
        );
        assertThat("Failed precondition. Account was add but missing",
                accountManager.getAccountsByType(FakeAccount.TYPE).length > 0
        );
    }
}