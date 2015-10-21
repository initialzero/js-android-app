/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.db.migrate.Migration;
import com.jaspersoft.android.jaspermobile.test.support.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.support.TestResource;
import com.jaspersoft.android.jaspermobile.test.support.db.PermanentDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.ResourceDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.SqlTestResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
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
public class LegacyProfileMigrationTest {

    private ResourceDatabase resourceDatabase;
    private String insertMobileProfileSql;
    private SQLiteDatabase database;
    private Migration migration;
    private SqlTestResource insertMobilesProfileSql;

    @Before
    public void setup() {
        // Dirty hack in order to revert AccountSeed side effect
        AccountUtil.get(RuntimeEnvironment.application).removeAllAccounts();
        resourceDatabase = PermanentDatabase.create("jasper_mobile_db_1.9").prepare();
        database = resourceDatabase.open();
        migration = new MigrationV3.LegacyProfileMigration();

        insertMobileProfileSql = TestResource.get("insert_mobile_profile.sql").asString();
        insertMobilesProfileSql = SqlTestResource.get("insert_mobile_profiles.sql");
    }

    @After
    public void teardown() {
        resourceDatabase.close();
        resourceDatabase.delete();
    }

    @Test
    public void shouldRenameLegacyProfile() {
        resourceDatabase.performSql(insertMobileProfileSql);
        migration.migrate(database);

        Cursor cursor = queryProfile();

        assertCursor(cursor);
        assertThat(cursor.getString(cursor.getColumnIndex("alias")), is("Legacy Mobile Demo"));

        cursor.close();
    }

    @Test
    public void shouldNotUpdateUrl() {
        resourceDatabase.performSql(insertMobileProfileSql);
        migration.migrate(database);

        Cursor cursor = queryProfile();

        assertCursor(cursor);
        assertThat(cursor.getString(cursor.getColumnIndex("server_url")), is("http://mobiledemo.jaspersoft.com/jasperserver-pro"));

        cursor.close();
    }

    @Test
    public void shouldOnlyRenameLegacyDemo() {
        resourceDatabase.execSQLResource(insertMobilesProfileSql);
        migration.migrate(database);

        Cursor cursor = queryProfile();
        assertThat(cursor.getCount(), is(2));

        List<String> aliases = new ArrayList<String>(cursor.getCount());
        while (cursor.moveToNext()) {
            String alias = cursor.getString(cursor.getColumnIndex("alias"));
            aliases.add(alias);
        }

        assertThat(aliases, hasItems("Legacy Mobile Demo", "Not Demo"));
    }

    private void assertCursor(Cursor cursor) {
        assertThat(cursor, notNullValue());
        assertThat(cursor.getCount(), is(1));
        assertThat(cursor.moveToFirst(), is(true));
    }

    private Cursor queryProfile() {
        return database.query("server_profiles",
                new String[]{"_id", "alias", "server_url"},
                null, null, null, null, null);
    }

}
