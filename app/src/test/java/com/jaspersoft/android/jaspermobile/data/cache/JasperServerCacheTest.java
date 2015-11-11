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
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;

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
public class JasperServerCacheTest {
    AccountManager accountManager;
    JasperServerCacheImpl cacheUnderTest;
    Profile fakeProfile;
    JasperServer fakeServer;
    FakeAccount fakeAccount;

    @Before
    public void setUp() throws Exception {
        accountManager = AccountManager.get(RuntimeEnvironment.application);
        fakeProfile = Profile.create("name");
        fakeServer = JasperServer.builder()
                .setBaseUrl("http://localhost")
                .setVersion(6.0d)
                .setEdition("CE")
                .create();
        fakeAccount = new FakeAccount(RuntimeEnvironment.application, fakeProfile);
        cacheUnderTest = new JasperServerCacheImpl(RuntimeEnvironment.application, FakeAccount.ACCOUNT_TYPE);
    }

    @Test
    public void testPut() throws Exception {
        fakeAccount.setup();
        cacheUnderTest.put(fakeProfile, fakeServer);

        Account account = fakeAccount.get();

        assertThat("Server url should be injected in cache",
                fakeServer.getBaseUrl().equals(accountManager.getUserData(account, "SERVER_URL_KEY")));
        assertThat("Edition should be injected in cache",
                fakeServer.getEdition().equals(accountManager.getUserData(account, "EDITION_KEY")));
        assertThat("Version should be injected in cache",
                String.valueOf(fakeServer.getVersion())
                        .equals(accountManager.getUserData(account, "VERSION_NAME_KEY")));
    }
}