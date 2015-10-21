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
import static org.hamcrest.core.Is.is;

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
    public static final String ACCOUNT_TYPE = "com.jaspersoft";
    private AccountManager accountManager;

    @Before
    public void setup() {
        // Dirty hack in order to revert AccountSeed side effect
        AccountUtil.get(RuntimeEnvironment.application).removeAllAccounts();

        accountManager = AccountManager.get(RuntimeEnvironment.application);
        migration = new MigrationV3.ProfilesToAccountsMigration(RuntimeEnvironment.application);
        resourceDatabase = PermanentDatabase.create("jasper_mobile_db_1.9").prepare();
        database = resourceDatabase.open();

        insertMobileProfileSql = SqlTestResource.get("insert_custom_profiles.sql");
    }

    @Test
    public void shouldConvertProfilesInAccount() {
        resourceDatabase.execSQLResource(insertMobileProfileSql);
        migration.migrate(database);

        List<Account> accountList = listAccounts();
        Account account1 = new Account("My profile 1", ACCOUNT_TYPE);
        Account account2 = new Account("My profile 2", ACCOUNT_TYPE);

        assertThat(accountList, hasItems(account1, account2));

        String alias1 = accountManager.getUserData(account1, "ALIAS_KEY");
        assertThat(alias1, is("My profile 1"));

        String serverUrl = accountManager.getUserData(account1, "SERVER_URL_KEY");
        assertThat(serverUrl, is("http://profile1.com/jasperserver-pro"));

        String organization = accountManager.getUserData(account1, "ORGANIZATION_KEY");
        assertThat(organization, is("organization_1"));

        String username = accountManager.getUserData(account1, "USERNAME_KEY");
        assertThat(username, is("phoneuser"));

        String edition = accountManager.getUserData(account1, "EDITION_KEY");
        assertThat(edition, is("PRO"));

        String version = accountManager.getUserData(account1, "VERSION_NAME_KEY");
        assertThat(version, is("5.5"));

        String alias2 = accountManager.getUserData(account2, "ALIAS_KEY");
        assertThat(alias2, is("My profile 2"));
    }

    @Test
    public void shouldInsertDefaultVersionNameIfOneIsMissing() {
        String profileWithoutVersionSQL = "INSERT INTO `server_profiles`(`alias`,`server_url`,`organization`,`username`,`password`,`edition`,`version_code`) VALUES ('My profile 1','http://profile1.com/jasperserver-pro','organization_1','phoneuser','phoneuser','PRO', NULL);";
        resourceDatabase.performSql(profileWithoutVersionSQL);
        migration.migrate(database);

        Account account1 = new Account("My profile 1", ACCOUNT_TYPE);
        List<Account> accountList = listAccounts();
        assertThat(accountList, hasItems(account1));

        String version = accountManager.getUserData(account1, "VERSION_NAME_KEY");
        assertThat(version, is("0.0"));
    }

    @Test
    public void shouldInsertDefaultEditionIfOneIsMissing() {
        String profileWithoutEditionSQL = "INSERT INTO `server_profiles`(`alias`,`server_url`,`organization`,`username`,`password`,`edition`,`version_code`) VALUES ('My profile 1','http://profile1.com/jasperserver-pro','organization_1','phoneuser','phoneuser',NULL, 5.5);";
        resourceDatabase.performSql(profileWithoutEditionSQL);
        migration.migrate(database);

        Account account1 = new Account("My profile 1", ACCOUNT_TYPE);
        List<Account> accountList = listAccounts();
        assertThat(accountList, hasItems(account1));

        String version = accountManager.getUserData(account1, "EDITION_KEY");
        assertThat(version, is("?"));
    }

    private List<Account> listAccounts() {
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        return Arrays.asList(accounts);
    }
}
