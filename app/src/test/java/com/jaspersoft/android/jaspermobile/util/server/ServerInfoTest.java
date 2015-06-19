/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.server;

import android.accounts.Account;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ServerInfoTest {
    @Test
    public void shouldProvideServerVersion() {
        AccountServerData serverData = new AccountServerData()
                .setAlias("alias")
                .setServerUrl("")
                .setOrganization("")
                .setUsername("")
                .setPassword("")
                .setEdition("CE")
                .setVersionName("5.5");

        JasperAccountManager jasperAccountManager = JasperAccountManager.get(RuntimeEnvironment.application);
        Account account = jasperAccountManager.addAccountExplicitly(serverData).toBlocking().first();
        jasperAccountManager.activateAccount(account);

        ServerInfoProvider serverInfoProvider =
                ServerInfo.newInstance(RuntimeEnvironment.application);
        String retrievedVersion = serverInfoProvider.getServerVersion();

        assertThat(retrievedVersion, is("5.5"));
    }
}
