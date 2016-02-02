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

package com.jaspersoft.android.jaspermobile.db.migrate.v5;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.jaspersoft.android.jaspermobile.db.migrate.Migration;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

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
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21, shadows = {ShadowMultiDex.class})
public class MigrationV5Test {
    private static final String EDITION_KEY = "EDITION_KEY";
    private static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";

    private Migration migrationV5;
    private Account fakeAccount;
    private AccountManager accountManager;

    @Before
    public void setup() {
        fakeAccount = new Account("TEST", JasperSettings.JASPER_ACCOUNT_TYPE);
        accountManager = AccountManager.get(RuntimeEnvironment.application);
        accountManager.setUserData(fakeAccount, EDITION_KEY, "PRO");
        accountManager.setUserData(fakeAccount, VERSION_NAME_KEY, "5.6.1");
        accountManager.addAccountExplicitly(fakeAccount, null, null);

        migrationV5 = new MigrationV5();
    }

    // TODO Fix test by providing dump of database
    @Test
    public void migrateEdition() {
        migrationV5.migrate(null);
        String edition = accountManager.getUserData(fakeAccount, EDITION_KEY);
        assertThat(edition, is("true"));
    }
}
