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

package com.jaspersoft.android.jaspermobile.db.migrate.v3;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.db.migrate.Migration;
import com.jaspersoft.android.jaspermobile.test.support.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.support.db.PermanentDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.ResourceDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.SqlTestResource;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = 21
)
public class ProfilesToAccountsMigrationTest {

    private ResourceDatabase resourceDatabase;
    private ResourceDatabase.RawSqlStatements insertMobileProfileSql;
    private Migration migration;
    private SQLiteDatabase database;

    @Before
    public void setup() {
        // Dirty hack in order to revert AccountSeed side effect
        AccountUtil.get(RuntimeEnvironment.application).removeAllAccounts();

        migration = new ProfilesToAccountsMigration(RuntimeEnvironment.application);
        resourceDatabase = PermanentDatabase.create("jasper_mobile_db_1.9").prepare();
        database = resourceDatabase.open();

        insertMobileProfileSql = SqlTestResource.get("insert_custom_profiles.sql");
    }

    @Test
    public void shouldConvertProfilesInAccount() {
        resourceDatabase.execSQLResource(insertMobileProfileSql);
        migration.migrate(database);

        Account[] accounts = AccountManager.get(RuntimeEnvironment.application).getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        List<Account> accountList = Arrays.asList(accounts);
        Account account1 = new Account("My profile 1", JasperSettings.JASPER_ACCOUNT_TYPE);
        Account account2 = new Account("My profile 2", JasperSettings.JASPER_ACCOUNT_TYPE);

        assertThat(accountList, hasItems(account1, account2));
    }

}
