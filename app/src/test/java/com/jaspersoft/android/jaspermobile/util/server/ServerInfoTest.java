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
import android.provider.Settings;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ServerInfoTest {

    AccountServerData fakeServerData;

    @Before
    public void setup() {
        Settings.Secure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ANDROID_ID, "ROBOLECTRICYOUAREBAD");
        fakeServerData = new AccountServerData()
                .setAlias("alias")
                .setServerUrl("")
                .setOrganization("")
                .setUsername("")
                .setPassword("")
                .setEdition("CE")
                .setVersionName("5.5");
    }

    @Test
    public void shouldProvideServerVersion() {
        activateServerData(fakeServerData);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedVersion = serverInfoProvider.getServerVersion();

        assertThat(retrievedVersion, is("5.5"));
    }

    @Test
    public void shouldProvideServerEdition() {
        activateServerData(fakeServerData);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedEdition = serverInfoProvider.getServerEdition();

        assertThat(retrievedEdition, is("CE"));
    }

    @Test
    public void shouldProvideOrganization() {
        fakeServerData.setOrganization("organization");
        activateServerData(fakeServerData);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedOrganization = serverInfoProvider.getOrganization();

        assertThat(retrievedOrganization, is("organization"));
    }

    @Test
    public void shouldProvideEmptyOrganizationIfOneMissing() {
        fakeServerData.setOrganization(null);
        activateServerData(fakeServerData);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedOrganization = serverInfoProvider.getOrganization();

        assertThat(retrievedOrganization, is(""));
    }

    @Test
    public void shouldProvideUsername() {
        fakeServerData.setUsername("username");
        activateServerData(fakeServerData);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedUsername = serverInfoProvider.getUsername();

        assertThat(retrievedUsername, is("username"));
    }

    @Test
    public void shouldProvideAlias() {
        fakeServerData.setAlias("alias");
        activateServerData(fakeServerData);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedAlias = serverInfoProvider.getAlias();

        assertThat(retrievedAlias, is("alias"));
    }

    private void activateServerData(AccountServerData serverData) {
        JasperAccountManager jasperAccountManager = JasperAccountManager.get(RuntimeEnvironment.application);
        Account account = jasperAccountManager.addAccountExplicitly(serverData).toBlocking().first();
        jasperAccountManager.activateAccount(account);
    }
}
