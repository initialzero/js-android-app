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
import android.app.Activity;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.2.2
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21, shadows = {ShadowMultiDex.class})
public class ActiveAccountStorageTest {

    private SharedPreferences fakePref;
    private ActiveAccountStorage cache;

    @Before
    public void setUp() throws Exception {
        fakePref = RuntimeEnvironment.application.getSharedPreferences("test", Activity.MODE_PRIVATE);
        cache = new ActiveAccountStorage(fakePref, "com.test");
    }

    @Test
    public void testPut() throws Exception {
        cache.put(new Account("test", "com.test"));
        String name = fakePref.getString(ActiveAccountStorage.KEY, null);
        assertThat("Failed to save account in cache", "test".equals(name));
    }

    @Test
    public void testGet() throws Exception {
        fakePref.edit().putString(ActiveAccountStorage.KEY, "foo").apply();
        Account account = cache.get();
        assertThat("Failed to retrieve account from cache", account != null);
        assertThat(account.name, is("foo"));
        assertThat(account.type, is("com.test"));
    }

    @Test
    public void testGetShouldReturnNullIfAccountMissing() throws Exception {
        Account account = cache.get();
        assertThat("There should be no account", account == null);
    }

    @Test
    public void testClear() throws Exception {
        fakePref.edit().putString(ActiveAccountStorage.KEY, "foo").apply();
        cache.clear();
        String name = fakePref.getString(ActiveAccountStorage.KEY, null);
        assertThat("Failed to remove account from cache", name == null);
    }
}