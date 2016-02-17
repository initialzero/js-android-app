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

package com.jaspersoft.android.jaspermobile.util.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Pair;

import com.jaspersoft.android.jaspermobile.data.FakeAccountDataMapper;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.orhanobut.hawk.Storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author Tom Koptel
 * @since 2.2.2
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class AccountStorageTest {

    private static final String PASSWORD = "password";
    private Storage accountStorage;

    @Mock
    ActiveProfileCache mActiveProfileCache;

    private AccountManager accountManager;
    private final Profile fakeProfile = Profile.create("fake");
    private Account fakeAccount;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();

        accountManager = spy(AccountManager.get(RuntimeEnvironment.application));
        fakeAccount = new Account(fakeProfile.getKey(), FakeAccountDataMapper.TYPE);

        AccountDataMapper fakeAccountDataMapper = FakeAccountDataMapper.get();
        accountStorage = new AccountStorage(
                accountManager,
                fakeAccountDataMapper,
                mActiveProfileCache
        );
    }

    private void setupMocks() {
        when(mActiveProfileCache.get()).thenReturn(fakeProfile);
    }

    @Test
    public void should_store_password_in_account() throws Exception {
        whenPutsWithKey(createPasswordKey());

        thenShouldPutPasswordInAccountManager();
    }

    private String createPasswordKey() {
        return AccountStorage.KEY + fakeAccount.name;
    }

    @Test
    public void should_store_password_for_personilzz_account_name() throws Exception {
        whenPutsWithKey(AccountStorage.KEY + fakeProfile.getKey());

        thenShouldPutPasswordInAccountManager();
    }

    private void whenPutsWithKey(String key) {
        accountStorage.put(key, PASSWORD);
    }

    private void thenShouldPutPasswordInAccountManager() {
        Account account = new Account(fakeProfile.getKey(), FakeAccountDataMapper.TYPE);
        verify(accountManager).setPassword(eq(account), eq(PASSWORD));
    }


    @Test
    public void testPut() throws Exception {
        accountStorage.put("bar", "foo");
        String bar = accountManager.getUserData(fakeAccount, "bar");
        assertThat("Failed to put single value in account storage", bar != null);
    }

    @Test
    public void testPutPairs() throws Exception {
        Pair<String, ?> pair1 = Pair.create("bar", "foo");
        Pair<String, ?> pair2 = Pair.create("foo", "bar");
        List<Pair<String, ?>> pairs = Arrays.asList(pair1, pair2);

        accountStorage.put(pairs);

        String foo = accountManager.getUserData(fakeAccount, "foo");
        String bar = accountManager.getUserData(fakeAccount, "bar");
        assertThat(
                "Failed to put list of values in account storage",
                foo != null && bar != null
        );
    }

    @Test
    public void testGet() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        String bar = accountStorage.get("foo");
        assertThat(
                "Failed to get value from account storage",
                bar != null
        );
    }

    @Test
    public void testRemove() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        accountStorage.remove("foo");
        String foo = accountManager.getUserData(fakeAccount, "foo");
        assertThat(
                "Failed to remove value from account storage",
                foo == null
        );
    }



    @Test
    public void testRemoveByKeys() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");
        accountManager.setUserData(fakeAccount, "bar", "foo");

        accountStorage.remove("foo", "bar");

        String foo = accountManager.getUserData(fakeAccount, "foo");
        String bar = accountManager.getUserData(fakeAccount, "bar");
        assertThat(
                "Failed to remove list of values from account storage",
                foo == null && bar == null
        );
    }

    @Test
    public void testContains() throws Exception {
        accountManager.setUserData(fakeAccount, "foo", "bar");

        assertThat(
                "Contains 'foo' key condition failed",
                accountStorage.contains("foo")
        );
    }

    @Test
    public void testContainsPassword() throws Exception {
        accountManager.setPassword(fakeAccount, "foo");

        assertThat(
                "Contains password condition failed",
                accountStorage.contains(createPasswordKey())
        );
    }
}