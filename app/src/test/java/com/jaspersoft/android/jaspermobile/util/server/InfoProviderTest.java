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

package com.jaspersoft.android.jaspermobile.util.server;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21, constants = BuildConfig.class, shadows = {ShadowMultiDex.class})
public class InfoProviderTest {
    private static final String ALIAS_KEY = "ALIAS_KEY";
    private static final String SERVER_URL_KEY = "SERVER_URL_KEY";
    private static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    private static final String USERNAME_KEY = "USERNAME_KEY";

    private static final String EDITION_KEY = "EDITION_KEY";
    private static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";

    private InfoProvider serverInfoProvider;
    private Account fakeAccount;
    private AccountManager accountManager;

    @Before
    public void setup() {
        Settings.Secure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ANDROID_ID, "ROBOLECTRICYOUAREBAD");

        Context context = RuntimeEnvironment.application;

        fakeAccount = new Account("TEST", "com.jaspersoft");
        accountManager = AccountManager.get(RuntimeEnvironment.application);
        accountManager.addAccountExplicitly(fakeAccount, null, Bundle.EMPTY);
        accountManager.setUserData(fakeAccount, ALIAS_KEY, "alias");
        accountManager.setUserData(fakeAccount, SERVER_URL_KEY, "http://localhost");
        accountManager.setUserData(fakeAccount, ORGANIZATION_KEY, "organization");
        accountManager.setUserData(fakeAccount, USERNAME_KEY, "username");
        accountManager.setUserData(fakeAccount, EDITION_KEY, "true");
        accountManager.setUserData(fakeAccount, VERSION_NAME_KEY, "5.5");

        SharedPreferences pref = context.getSharedPreferences("JasperAccountManager", Activity.MODE_PRIVATE);
        pref.edit().putString("ACCOUNT_NAME_KEY", fakeAccount.name).apply();

        serverInfoProvider = new InfoProvider(context);
    }

    @Test
    public void shouldProvideServerVersion() {
        ServerVersion retrievedVersion = serverInfoProvider.getVersion();
        assertThat(retrievedVersion, is(ServerVersion.v5_5));
    }

    @Test
    public void shouldProvideServerEdition() {
        boolean retrievedEdition = serverInfoProvider.isProEdition();
        assertThat(retrievedEdition, is(true));
    }

    @Test
    public void shouldProvideOrganization() {
        String retrievedOrganization = serverInfoProvider.getOrganization();
        assertThat(retrievedOrganization, is("organization"));
    }

    @Test
    public void shouldProvideUsername() {
        String retrievedUsername = serverInfoProvider.getUsername();
        assertThat(retrievedUsername, is("username"));
    }

    @Test
    public void shouldProvideAlias() {
        String retrievedAlias = serverInfoProvider.getAlias();
        assertThat(retrievedAlias, is("alias"));
    }

    @Test
    public void shouldProvideServerUrl() {
        String retrievedAlias = serverInfoProvider.getServerUrl();
        assertThat(retrievedAlias, is("http://localhost"));
    }
}
