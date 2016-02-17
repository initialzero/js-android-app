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

package com.jaspersoft.android.jaspermobile.db.migrate;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.test.support.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.support.db.PermanentDatabase;
import com.jaspersoft.android.jaspermobile.test.support.db.ResourceDatabase;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, shadows = {ShadowApplicationImpl.class})
public class SavedItemsMigrationV3Test {
    private static final String SHARED_DIR = "com.jaspersoft.account.none";

    private ResourceDatabase resourceDatabase;
    private SQLiteDatabase database;
    private Migration migration;

    @Before
    public void setup() {
        // Dirty hack in order to revert AccountSeed side effect
        AccountUtil.get(RuntimeEnvironment.application).removeAllAccounts();
        resourceDatabase = PermanentDatabase.create("jasper_mobile_db_1.9").prepare();
        database = resourceDatabase.open();
        migration = new MigrationV3.SavedItemsMigration(RuntimeEnvironment.application);
    }

    @After
    public void teardown() {
        resourceDatabase.close();
        resourceDatabase.delete();
        removeExternalDir();
    }

    @Test
    public void savedItemsShouldBeMigratedToSharedDir() throws IOException {
        File savedReportsDir = createSavedReportsDir();
        populateSavedReportsDir(savedReportsDir);

        migration.migrate(database);

        File sharedSavedItemsFolder = new File(savedReportsDir, SHARED_DIR);
        assertThat(sharedSavedItemsFolder.exists(), is(true));

        assertThat(sharedSavedItemsFolder.listFiles().length, is(2));
    }

    @Test
    public void savedItemsShouldBeMigratedToDatabase() throws IOException {
        File savedItemsDir = createSavedReportsDir();
        populateSavedReportsDir(savedItemsDir);

        migration.migrate(database);

        Cursor cursor = database.query("saved_items",
                new String[]{"_id", "file_path", "name", "file_format", "wstype", "account_name", "creation_time"},
                null, null, null, null, null);
        assertThat(cursor, notNullValue());
        assertThat(cursor.getCount(), is(2));

        File sharedDir = new File(savedItemsDir, SHARED_DIR);
        File[] savedReportsDirs = sharedDir.listFiles();

        for (int i = 0; i < savedReportsDirs.length; i++) {
            File reportDir = savedReportsDirs[i];
            File report = new File(reportDir, reportDir.getName());
            assertThat(cursor.moveToPosition(i), is(true));

            String fileName = FileUtils.getBaseName(report.getName());
            String fileFormat = FileUtils.getExtension(report.getName()).toUpperCase(Locale.getDefault());
            long creationTime = reportDir.lastModified();

            assertThat(reportDir.listFiles().length, is(2));
            assertThat(cursor.getString(cursor.getColumnIndex("file_path")), is(report.getPath()));
            assertThat(cursor.getString(cursor.getColumnIndex("name")), is(fileName));
            assertThat(cursor.getString(cursor.getColumnIndex("file_format")), is(fileFormat));
            assertThat(cursor.getString(cursor.getColumnIndex("wstype")), is("unknown"));
            assertThat(cursor.getString(cursor.getColumnIndex("account_name")), is("com.jaspersoft.account.none"));
            assertThat(cursor.getString(cursor.getColumnIndex("creation_time")), is(String.valueOf(creationTime)));
        }
    }

    private void populateSavedReportsDir(File savedReportsDir) throws IOException {
        File savedItemDir1 = new File(savedReportsDir, "report1.html");
        File savedItem1 = new File(savedItemDir1, "report1.html");
        File savedItemImage1 = new File(savedItemDir1, "image1.jpg");
        assertThat(savedItemDir1.mkdir(), is(true));
        assertThat(savedItem1.createNewFile(), is(true));
        assertThat(savedItemImage1.createNewFile(), is(true));

        File savedItemDir2 = new File(savedReportsDir, "report2.html");
        File savedItem2 = new File(savedItemDir2, "report2.html");
        File savedItemImage2 = new File(savedItemDir2, "image2.jpg");
        assertThat(savedItemDir2.mkdir(), is(true));
        assertThat(savedItem2.createNewFile(), is(true));
        assertThat(savedItemImage2.createNewFile(), is(true));
    }

    private File createSavedReportsDir() {
        File externalDir = RuntimeEnvironment.application.getExternalFilesDir(null);
        File savedReportsDir = new File(externalDir, "saved.reports");
        if (!savedReportsDir.exists()) {
            assertThat(savedReportsDir.mkdir(), is(true));
        }
        return savedReportsDir;
    }

    private void removeExternalDir() {
        File externalDir = RuntimeEnvironment.application.getExternalFilesDir(null);
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(externalDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
