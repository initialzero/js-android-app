/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jaspersoft.android.jaspermobile.db.JSDatabaseHelper;
import com.jaspersoft.android.jaspermobile.test.support.CustomRobolectricTestRunner;
import com.jaspersoft.android.jaspermobile.util.GeneralPref_;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.jaspersoft.android.jaspermobile.db.migrate.SavedItemsMigration.SHARED_DIR;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(
        shadows = {ShadowApplicationImpl.class},
        emulateSdk = 18
)
public class Migrate_1_9_to_2_0Test {
    private WrapperOpenHelper helper;

    private static final String INSERT_PROFILE = "INSERT INTO `server_profiles`(`alias`,`server_url`,`organization`,`username`,`password`,`edition`,`version_code`)" +
            " VALUES ('Mobile Demo','http://mobiledemo.jaspersoft.com/jasperserver-pro','organization_1','phoneuser','phoneuser','PRO',5.5);";
    private static final String INSERT_MIS_CONFIGURED_PROFILE = "INSERT INTO `server_profiles`(`alias`,`server_url`,`organization`,`username`,`password`,`edition`,`version_code`)" +
            " VALUES ('Mobile Demo','http://mobiledemo.jaspersoft.com/jasperserver-pro','organization_1','phoneuser','phoneuser',null,null);";
    private static final String INSERT_FAVORITE = "INSERT INTO `favorites`(`name`,`title`,`uri`,`description`,`wstype`,`username`,`organization`,`server_profile_id`) " +
            "VALUES (NULL, '01. Geographic Results by Segment Report','/Reports/1._Geographic_Results_by_Segment_Report','Description','reportUnit','phoneuser','organization_1',1);";
    private SQLiteDatabase newDb;
    private Cursor cursor;
    private boolean migrated;

    @Before
    public void recreateDatabase() {
        resetDatabases();
        populateOldDatabase();
    }

    @After
    public void cleanUp() {
        // Ensure the migration has been performed in the test
        assertThat(migrated, is(true));
        migrated = false;
        releaseResources();
    }

    //---------------------------------------------------------------------
    // Server profiles/accounts migration
    //---------------------------------------------------------------------

    @Test
    public void shouldSkipsMisConfiguredServerProfiles() {
        execSQLOverOldDb(INSERT_MIS_CONFIGURED_PROFILE);
        performMigrations();

        Account[] accounts = JasperAccountManager.get(Robolectric.application).getAccounts();
        List<Account> accountList = Arrays.asList(accounts);
        assertThat(accountList.size(), is(1));
    }

    @Test
    public void testServerProfilesTableMigration() throws Exception {
        performMigrations();
        cursor = newDb.query("server_profiles",
                new String[]{"_id", "alias", "server_url", "organization", "username", "password", "edition", "version_code"},
                null, null, null, null, null);
        assertThat(cursor, notNullValue());
        assertThat(cursor.getCount(), is(1));
    }

    @Test
    public void testShouldConvertProfilesInAccount() {
        performMigrations();
        Account[] accounts = JasperAccountManager.get(Robolectric.application).getAccounts();
        List<Account> accountList = Arrays.asList(accounts);
        Account accountToAssert = new Account("Mobile Demo", JasperSettings.JASPER_ACCOUNT_TYPE);
        assertThat(accountList.size(), not(0));
        assertThat(accountList, hasItem(accountToAssert));
    }

    @Test
    public void testShouldActivateActiveProfileAsAccount() {
        activateFirstProfile();
        performMigrations();

        Account account = JasperAccountManager.get(Robolectric.application).getActiveAccount();
        assertThat(account, CoreMatchers.notNullValue());
    }

    //---------------------------------------------------------------------
    // Favorites migration
    //---------------------------------------------------------------------

    @Test
    public void testFavoritesTableMigration() throws Exception {
        performMigrations();
        cursor = newDb.query("favorites",
                new String[]{"_id", "title", "uri", "description", "wstype", "username", "organization", "account_name", "creation_time"},
                null, null, null, null, null);
        assertThat(cursor, notNullValue());
        assertThat(cursor.getCount(), is(1));
        assertThat(cursor.moveToFirst(), is(true));

        assertThat(cursor.getString(cursor.getColumnIndex("title")), is("01. Geographic Results by Segment Report"));
        assertThat(cursor.getString(cursor.getColumnIndex("uri")), is("/Reports/1._Geographic_Results_by_Segment_Report"));
        assertThat(cursor.getString(cursor.getColumnIndex("description")), is("Description"));
        assertThat(cursor.getString(cursor.getColumnIndex("wstype")), is("reportUnit"));
        assertThat(cursor.getString(cursor.getColumnIndex("username")), is("phoneuser"));
        assertThat(cursor.getString(cursor.getColumnIndex("organization")), is("organization_1"));
        assertThat(cursor.getString(cursor.getColumnIndex("account_name")), is("Mobile Demo"));
    }

    //---------------------------------------------------------------------
    // Saved items migration
    //---------------------------------------------------------------------

    @Test
    public void savedItemsShouldBeMigratedToSharedDir() throws IOException {
        File savedReportsDir = prepareSavedReportsDir();
        populateSavedReportsDir(savedReportsDir);

        performMigrations();

        File sharedSavedItemsFolder = new File(savedReportsDir, SHARED_DIR);
        assertThat(sharedSavedItemsFolder.exists(), is(true));

        assertThat(sharedSavedItemsFolder.listFiles().length, is(2));
    }

    @Test
    public void savedItemsShouldBeMigratedToDatabase() throws IOException {
        File savedItemsDir = prepareSavedReportsDir();
        populateSavedReportsDir(savedItemsDir);

        performMigrations();

        cursor = newDb.query("saved_items",
                new String[]{"_id", "file_path", "name", "file_format", "wstype", "account_name", "creation_time"},
                null, null, null, null, null);
        assertThat(cursor, notNullValue());
        assertThat(cursor.getCount(), is(2));

        File sharedDir = new File(savedItemsDir, SHARED_DIR);
        File[] savedReports = sharedDir.listFiles();

        for (int i = 0; i < savedReports.length; i++) {
            File report = savedReports[i];
            assertThat(cursor.moveToPosition(i), is(true));

            String fileName = FileUtils.getBaseName(report.getName());
            String fileFormat = FileUtils.getExtension(report.getName()).toUpperCase(Locale.getDefault());
            long creationTime = report.lastModified();

            assertThat(cursor.getString(cursor.getColumnIndex("file_path")), is(report.getPath()));
            assertThat(cursor.getString(cursor.getColumnIndex("name")), is(fileName));
            assertThat(cursor.getString(cursor.getColumnIndex("file_format")), is(fileFormat));
            assertThat(cursor.getString(cursor.getColumnIndex("wstype")), is("unknown"));
            assertThat(cursor.getString(cursor.getColumnIndex("account_name")), is("com.jaspersoft.account.none"));
            assertThat(cursor.getString(cursor.getColumnIndex("creation_time")), is(String.valueOf(creationTime)));
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void populateSavedReportsDir(File savedReportsDir) throws IOException {
        File savedItem1 = new File(savedReportsDir, "report1.html");
        assertThat(savedItem1.mkdir(), is(true));

        File savedItem2 = new File(savedReportsDir, "report2.html");
        assertThat(savedItem2.mkdir(), is(true));
    }

    private File prepareSavedReportsDir() {
        resetExternalDir();

        File externalDir = Robolectric.application.getExternalFilesDir(null);
        File savedReportsDir = new File(externalDir, "saved.reports");
        if (!savedReportsDir.exists()) {
            assertThat(savedReportsDir.mkdir(), is(true));
        }
        return savedReportsDir;
    }

    private void resetExternalDir() {
        File externalDir = Robolectric.application.getExternalFilesDir(null);
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(externalDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertThat(externalDir.mkdir(), is(true));
    }

    private void activateFirstProfile() {
        // Ensure method executed before migration
        assertThat(migrated, is(false));

        GeneralPref_ pref = new GeneralPref_(Robolectric.application);
        pref.currentProfileId().put(1);
    }

    private void assertInitialDB(SQLiteDatabase db) {
        assertThat(db, notNullValue());
        assertThat(db.isOpen(), is(true));
        assertThat(helper.onOpenCalled, is(true));
        assertThat(helper.onUpgradeCalled, is(true));
    }

    private void performMigrations() {
        // Ensure migration has been performed only one time
        assertThat(migrated, is(false));
        migrated = true;
        helper = new WrapperOpenHelper(Robolectric.application);
        newDb = helper.getReadableDatabase();
        // Assert new database was initialized
        assertInitialDB(newDb);
    }

    private void populateOldDatabase() {
        execSQLOverOldDb(INSERT_PROFILE);
        execSQLOverOldDb(INSERT_FAVORITE);
    }

    private void execSQLOverOldDb(String sql) {
        SQLiteOpenHelper_1_9 sqLiteOpenHelper_1_9 = new SQLiteOpenHelper_1_9(Robolectric.application);
        SQLiteDatabase oldDb = sqLiteOpenHelper_1_9.getWritableDatabase();
        oldDb.execSQL(sql);
        oldDb.close();
    }

    private void resetDatabases() {
        File dbFile = Robolectric.application.getDatabasePath(null);
        assertThat(SQLiteDatabase.deleteDatabase(dbFile), is(true));
        Robolectric.application.getDatabasePath(null);
    }

    private void releaseResources() {
        if (newDb != null && newDb.isOpen()) {
            newDb.close();
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class SQLiteOpenHelper_1_9 extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "jasper_mobile_db";

        public SQLiteOpenHelper_1_9(final Context context) {
            super(context, DATABASE_NAME, null, 3);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private class WrapperOpenHelper extends JSDatabaseHelper {
        public boolean onUpgradeCalled;
        public boolean onOpenCalled;

        public WrapperOpenHelper(Context context) {
            super(context);
            reset();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onUpgrade(db, oldVersion, newVersion);
            onUpgradeCalled = true;
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            onOpenCalled = true;
        }

        public void reset() {
            onUpgradeCalled = false;
            onOpenCalled = false;
        }
    }
}
