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

package com.jaspersoft.android.jaspermobile.migrate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.db.JSDatabaseHelper;
import com.jaspersoft.android.jaspermobile.test.support.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.support.db.MigrationCondition;
import com.jaspersoft.android.jaspermobile.test.support.db.MigrationConditionFactory;
import com.jaspersoft.android.jaspermobile.test.support.db.PermanentDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.ResourceDatabase;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = 21
)
public class MigrateProfiles1_9to2_0Test {
    private static final String DB_NAME = "jasper_mobile_db_1.9";

    private static final String INSERT_PROFILE = "INSERT INTO `server_profiles`(`alias`,`server_url`,`organization`,`username`,`password`,`edition`,`version_code`)" +
            " VALUES ('Mobile Demo','http://mobiledemo.jaspersoft.com/jasperserver-pro','organization_1','phoneuser','phoneuser','PRO',5.5);";
    private static final String INSERT_MIS_CONFIGURED_PROFILE = "INSERT INTO `server_profiles`(`alias`,`server_url`,`organization`,`username`,`password`,`edition`,`version_code`)" +
            " VALUES ('Mobile Demo','http://mobiledemo.jaspersoft.com/jasperserver-pro','organization_1','phoneuser','phoneuser',null,null);";

    private ResourceDatabase resourceDatabase;
    private JSDatabaseHelper databaseHelper;
    private MigrationCondition migrationCondition;

    @Before
    public void setup() {
        // Dirty hack in order to revert AccountSeed side effect
        AccountUtil.get(RuntimeEnvironment.application).removeAllAccounts();
        resourceDatabase = PermanentDatabase.create(DB_NAME).prepare();
        databaseHelper = new JSDatabaseHelper(RuntimeEnvironment.application);
        migrationCondition = MigrationConditionFactory.conditionForAppVersion("1.9");
    }

    @After
    public void teardown() {
        resourceDatabase.delete();
    }

    @Test
    public void testServerProfilesTableMigration() throws Exception {
        resourceDatabase.performSql(INSERT_PROFILE);
        resourceDatabase.performMigration(databaseHelper, migrationCondition);

        resourceDatabase.performAction(new ResourceDatabase.DbAction() {
            @Override
            public void performAction(SQLiteDatabase database) {
                Cursor cursor = database.query("server_profiles",
                        new String[]{"_id", "alias", "server_url", "organization", "username", "password", "edition", "version_code"},
                        null, null, null, null, null);

                assertThat(cursor, notNullValue());
                assertThat(cursor.getCount(), is(1));

                cursor.close();
            }
        });
    }

    @Test
    public void shouldSkipMisConfiguredServerProfiles() {
        resourceDatabase.performSql(INSERT_MIS_CONFIGURED_PROFILE);
        resourceDatabase.performMigration(databaseHelper, migrationCondition);

        Account[] accounts = AccountManager.get(RuntimeEnvironment.application).getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        List<Account> accountList = Arrays.asList(accounts);
        assertThat(accountList.size(), is(1));
    }

    @Test
    public void shouldConvertProfilesInAccount() {
        resourceDatabase.performSql(INSERT_PROFILE);
        resourceDatabase.performMigration(databaseHelper, migrationCondition);

        Account[] accounts = AccountManager.get(RuntimeEnvironment.application).getAccountsByType(JasperSettings.JASPER_ACCOUNT_TYPE);
        List<Account> accountList = Arrays.asList(accounts);
        Account accountToAssert = new Account("Mobile Demo", JasperSettings.JASPER_ACCOUNT_TYPE);

        assertThat(accountList.size(), not(0));
        assertThat(accountList, hasItem(accountToAssert));
    }

}
