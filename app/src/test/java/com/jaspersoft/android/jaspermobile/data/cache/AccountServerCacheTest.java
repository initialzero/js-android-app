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
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

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
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountServerCacheTest {
    AccountManager accountManager;
    AccountServerCache cacheUnderTest;
    Profile fakeProfile;
    JasperServer fakeServer;

    @Mock
    ProfileCache mProfileCache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        accountManager = AccountManager.get(RuntimeEnvironment.application);
        fakeProfile = Profile.create("name");
        fakeServer = JasperServer.builder()
                .setBaseUrl("http://localhost")
                .setVersion(ServerVersion.v6)
                .setEditionIsPro(false)
                .create();
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        cacheUnderTest = new AccountServerCache(accountManager, FakeAccountDataMapper.get(), mProfileCache);
    }

    @Test
    public void testPut() throws Exception {
        Account fakeAccount = FakeAccount.injectAccount(fakeProfile).done();
        cacheUnderTest.put(fakeProfile, fakeServer);

        String serverUrl = accountManager.getUserData(fakeAccount, "SERVER_URL_KEY");
        String edition = accountManager.getUserData(fakeAccount, "EDITION_KEY");
        String versionName = accountManager.getUserData(fakeAccount, "VERSION_NAME_KEY");

        assertThat("Server url should be injected in cache",
                fakeServer.getBaseUrl().equals(serverUrl));
        assertThat("Edition should be injected in cache",
                !fakeServer.isProEdition());
        assertThat("Version should be injected in cache",
                String.valueOf(fakeServer.getVersionName())
                        .equals(versionName));
    }

    @Test
    public void testGet() throws Exception {
        Account fakeAccount = FakeAccount.injectAccount(fakeProfile)
                .injectServer(fakeServer)
                .done();
        JasperServer server = cacheUnderTest.get(fakeProfile);

        assertThat("Failed to retrieve base url for profile " + fakeAccount,
                "http://localhost".equals(server.getBaseUrl()));
        assertThat("Failed to retrieve version for profile " + fakeAccount,
                server.getVersionName().equals("6.0"));
        assertThat("Failed to retrieve edition for profile " + fakeAccount,
                !server.isProEdition());
    }

    @Test
    public void testHasServerIfExistsAccountAndHasUserData() throws Exception {
        when(mProfileCache.hasProfile(any(Profile.class))).thenReturn(true);
        FakeAccount.injectAccount(fakeProfile)
                .injectServer(fakeServer)
                .done();

        assertThat("Cache should contain injected server",
                cacheUnderTest.hasServer(fakeProfile));

        verify(mProfileCache).hasProfile(fakeProfile);
    }

    @Test
    public void testServerMissingIfServerUrlMissing() throws Exception {
        when(mProfileCache.hasProfile(any(Profile.class))).thenReturn(true);
        FakeAccount.injectAccount(fakeProfile)
                .injectServer(
                        JasperServer.builder()
                                .setEditionIsPro(false)
                                .setVersion(ServerVersion.v5_5)
                                .create()
                ).done();


        assertThat("Cache should not contain injected server, because server url missing",
                !cacheUnderTest.hasServer(fakeProfile));
    }

    @Test
    public void testServerMissingIfEditionMissing() throws Exception {
        when(mProfileCache.hasProfile(any(Profile.class))).thenReturn(true);
        FakeAccount.injectAccount(fakeProfile)
                .injectServer(
                        JasperServer.builder()
                                .setBaseUrl("http://localhost/")
                                .setVersion(ServerVersion.v5_5)
                                .create()
                ).done();


        assertThat("Cache should not contain injected server, because edition missing",
                !cacheUnderTest.hasServer(fakeProfile));
    }

    @Test
    public void testServerMissingIfVersionMissing() throws Exception {
        when(mProfileCache.hasProfile(any(Profile.class))).thenReturn(true);
        FakeAccount.injectAccount(fakeProfile)
                .injectServer(
                        JasperServer.builder()
                                .setBaseUrl("http://localhost/")
                                .setEditionIsPro(false)
                                .create()
                ).done();


        assertThat("Cache should not contain injected server, because version missing",
                !cacheUnderTest.hasServer(fakeProfile));
    }
}