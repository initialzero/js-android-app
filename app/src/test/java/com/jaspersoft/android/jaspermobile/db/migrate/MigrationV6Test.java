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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.test.support.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.support.TestResource;
import com.jaspersoft.android.jaspermobile.test.support.db.PermanentDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.ResourceDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.jaspersoft.android.jaspermobile.test.support.JsAssertions.assertCursor;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MigrationV6Test {

    private ResourceDatabase resourceDatabase;
    private SQLiteDatabase database;

    @Before
    public void setup() {
        // Dirty hack in order to revert AccountSeed side effect
        AccountUtil.get(RuntimeEnvironment.application).removeAllAccounts();
        resourceDatabase = PermanentDatabase.create("jasper_mobile_db_v6").prepare();
        database = resourceDatabase.open();
        MigrationV6 migration = new MigrationV6();

        String insertSavedItemSql = TestResource.get("insert_saved_item.sql").asString();

        resourceDatabase.performSql(insertSavedItemSql);
        migration.migrate(database);
    }

    @After
    public void teardown() {
        resourceDatabase.close();
        resourceDatabase.delete();
    }

    @Test
    public void shouldAddDownloadedColumn() {
        Cursor cursor = database.query("saved_items",
                new String[]{"_id", "downloaded"},
                null, null, null, null, null);
        assertCursor(cursor);
        cursor.moveToFirst();

        boolean downloaded = cursor.getInt(cursor.getColumnIndexOrThrow("downloaded")) == 1;
        assertThat(downloaded, is(true));
    }

}
