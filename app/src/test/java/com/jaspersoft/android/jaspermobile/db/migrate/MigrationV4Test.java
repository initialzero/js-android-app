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

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.provider.Settings;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MigrationV4Test {

    @Before
    public void setup() {
        Settings.Secure.putString(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.ANDROID_ID, "ROBOLECTRICYOUAREBAD");
    }

    @Test
    public void shouldUpdateAllAccountsWithEncryptedPassword() {
        Account account = new Account("TEST", JasperSettings.JASPER_ACCOUNT_TYPE);
        AccountManager accountManager = AccountManager.get(RuntimeEnvironment.application);
        accountManager.addAccountExplicitly(account, "1234", null);

        MigrationV4 migrationV4 = new MigrationV4(RuntimeEnvironment.application);
        migrationV4.migrate(null);

        String password = accountManager.getPassword(account);
        assertThat(password, is(not("1234")));
    }
}
