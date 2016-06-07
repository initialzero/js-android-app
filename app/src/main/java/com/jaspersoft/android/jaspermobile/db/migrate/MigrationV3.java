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
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class MigrationV3 implements Migration {
    private final Context mContext;

    public MigrationV3(Context context) {
        mContext = context;
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        migrateProfiles(database);
        migrateFavorites(database);
        migrateSavedItems(database);

    }

    private void migrateProfiles(SQLiteDatabase database) {
        Timber.d("Start migrating profiles");
        new LegacyProfileMigration().migrate(database);
        Timber.d("Start migrating accounts");
        new ProfilesToAccountsMigration(mContext).migrate(database);
    }

    void migrateFavorites(SQLiteDatabase database) {
        Timber.d("Start migrating favorites");
        new FavoriteTableColumnsMigration().migrate(database);
        new ProfileFavoritesMigration().migrate(database);
    }

    private void migrateSavedItems(SQLiteDatabase database) {
        Timber.d("Start migrating saved items");
        new SavedItemsMigration(mContext).migrate(database);
    }

    final static class LegacyProfileMigration implements Migration {
        private static final String LEGACY_MOBILE_DEMO_ALIAS = "Mobile Demo";
        private static final String LEGACY_NAME = "Legacy Mobile Demo";

        @Override
        public void migrate(SQLiteDatabase database) {
            updateLegacyProfile(database);
        }

        private void updateLegacyProfile(SQLiteDatabase database) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("alias", LEGACY_NAME);
            database.update("server_profiles", contentValues, "alias=?", new String[] {LEGACY_MOBILE_DEMO_ALIAS});
        }
    }

    final static class ProfilesToAccountsMigration implements Migration {
        private final static String TABLE_NAME = "server_profiles";

        private final static String _ID = "_id";
        private final static String ALIAS = "alias";
        private final static String SERVER_URL = "server_url";
        private final static String ORGANIZATION = "organization";
        private final static String USERNAME = "username";
        private final static String PASSWORD = "password";
        private final static String EDITION = "edition";
        private final static String VERSION_CODE = "version_code";
        private final static String[] ALL_COLUMNS = new String[]{_ID, ALIAS, SERVER_URL, ORGANIZATION, USERNAME, PASSWORD, EDITION, VERSION_CODE};

        private final Context mContext;
        private final AccountManager mAccountManager;

        ProfilesToAccountsMigration(Context context) {
            mContext = context;
            mAccountManager = AccountManager.get(mContext);
        }

        @Override
        public void migrate(SQLiteDatabase database) {
            adaptProfilesToAccounts(database);
            activateAccount(database);
        }

        private void adaptProfilesToAccounts(SQLiteDatabase db) {
            Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        adaptProfile(cursor);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        private void adaptProfile(Cursor cursor) {
            String alias = cursor.getString(cursor.getColumnIndex(ALIAS));
            String serverUrl = cursor.getString(cursor.getColumnIndex(SERVER_URL));
            String organization = cursor.getString(cursor.getColumnIndex(ORGANIZATION));
            String username = cursor.getString(cursor.getColumnIndex(USERNAME));
            String password = cursor.getString(cursor.getColumnIndex(PASSWORD));

            double versionName = cursor.getDouble(cursor.getColumnIndex(VERSION_CODE));
            String edition = cursor.getString(cursor.getColumnIndex(EDITION));
            edition = TextUtils.isEmpty(edition) ? "?" : edition;

            Account account = new Account(alias, "com.jaspersoft");
            mAccountManager.addAccountExplicitly(account, password, null);

            mAccountManager.setUserData(account, "ALIAS_KEY", alias);
            mAccountManager.setUserData(account, "SERVER_URL_KEY", serverUrl);
            mAccountManager.setUserData(account, "ORGANIZATION_KEY", organization);
            mAccountManager.setUserData(account, "USERNAME_KEY", username);
            mAccountManager.setUserData(account, "EDITION_KEY", edition);
            mAccountManager.setUserData(account, "VERSION_NAME_KEY", String.valueOf(versionName));
        }

        private void activateAccount(SQLiteDatabase db) {
            SharedPreferences pref = mContext.getSharedPreferences("GeneralPref", 0);
            long profileId = pref.getLong("currentProfileId", 0);

            Cursor cursor = db.query(TABLE_NAME,
                    ALL_COLUMNS, _ID + "=" + profileId,
                    null, null, null, null);
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String alias = cursor.getString(cursor.getColumnIndex(ALIAS));
                    SharedPreferences preference = mContext.getSharedPreferences("JasperAccountManager", Activity.MODE_PRIVATE);
                    preference.edit().putString("ACCOUNT_NAME_KEY", alias).apply();
                }
            } finally {
                cursor.close();
            }
        }
    }


    static final class FavoriteTableColumnsMigration implements Migration {
        @Override
        public void migrate(SQLiteDatabase database) {
            addAccountNameColumn(database);
            addCreationTimeColumn(database);
        }

        private void addAccountNameColumn(SQLiteDatabase database) {
            database.execSQL("ALTER TABLE favorites ADD COLUMN account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none';");
        }

        private void addCreationTimeColumn(SQLiteDatabase database) {
            database.execSQL("ALTER TABLE favorites ADD COLUMN creation_time TEXT DEFAULT '';");
        }
    }

    final static class ProfileFavoritesMigration implements Migration {
        @Override
        public void migrate(SQLiteDatabase database) {
            populateAccountNameColumn(database);
            removeServerProfileIdColumn(database);
        }

        private void populateAccountNameColumn(SQLiteDatabase database) {
            Cursor profilesCursor = database.rawQuery("SELECT _id, alias FROM server_profiles", null);
            try {
                if (profilesCursor != null && profilesCursor.getCount() > 0) {
                    addAccountNameIntoFavoritesForProfile(profilesCursor, database);
                }
            } finally {
                if (profilesCursor != null) profilesCursor.close();
            }
        }

        private void addAccountNameIntoFavoritesForProfile(Cursor profilesCursor, SQLiteDatabase database) {
            ContentValues contentValues = new ContentValues();
            String id, alias;
            while (profilesCursor.moveToNext()) {
                id = profilesCursor.getString(profilesCursor.getColumnIndex("_id"));
                alias = profilesCursor.getString(profilesCursor.getColumnIndex("alias"));

                contentValues.clear();
                contentValues.put("account_name", alias);
                database.update("favorites", contentValues, "server_profile_id=?", new String[]{id});
            }
        }

        private void removeServerProfileIdColumn(SQLiteDatabase database) {
            database.execSQL("ALTER TABLE favorites RENAME TO tmp_favorites;");

            database.execSQL(
                    "CREATE TABLE favorites ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, uri TEXT, " +
                            "description TEXT, wstype TEXT, username TEXT, organization TEXT, account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none', creation_time TEXT )"
            );
            database.execSQL("INSERT INTO favorites(title, uri, description, wstype, username, organization, account_name, creation_time)" +
                    " select title, uri, description, wstype, username, organization, account_name, creation_time from tmp_favorites;");

            database.execSQL("DROP TABLE IF EXISTS tmp_favorites;");
        }
    }

    final static class SavedItemsMigration implements Migration {
        private static final String SAVED_REPORTS_DIR_NAME = "saved.reports";
        private static final String SHARED_DIR = "com.jaspersoft.account.none";

        private static final String TAG = SavedItemsMigration.class.getSimpleName();
        private final Context mContext;

        public SavedItemsMigration(Context context) {
            mContext = context;
            Timber.tag(TAG);
        }

        @Override
        public void migrate(SQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE saved_items ( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " file_path TEXT NOT NULL, name TEXT NOT NULL," +
                            " file_format TEXT NOT NULL, description TEXT, " +
                            "wstype TEXT NOT NULL DEFAULT 'unknown'," +
                            " account_name TEXT NOT NULL DEFAULT 'com.jaspersoft.account.none'," +
                            " creation_time NUMERIC NOT NULL )"
            );
            migrateSavedItems(database);
        }

        private void migrateSavedItems(SQLiteDatabase db) {
            File savedItemsDir = getSavedItemsDir();
            if (savedItemsDir != null) {
                File[] savedItems = savedItemsDir.listFiles();

                File sharedDir = createShareDirectory(savedItemsDir);
                if (sharedDir != null) {
                    if (savedItems.length > 0) {
                        moveToSharedDir(savedItems, sharedDir);
                        saveSharedFilesInDb(db, sharedDir);
                    }
                }
            }
        }

        @Nullable
        private File createShareDirectory(File savedItemsDir) {
            File sharedDir = new File(savedItemsDir, SHARED_DIR);
            if (!sharedDir.exists() && !sharedDir.mkdir()) return null;
            return sharedDir;
        }

        @Nullable
        private File getSavedItemsDir() {
            File appFilesDir = mContext.getExternalFilesDir(null);
            File savedReportsDir = new File(appFilesDir, SAVED_REPORTS_DIR_NAME);

            if (!savedReportsDir.exists()) {
                boolean created = savedReportsDir.mkdir();
                if (!created) {
                    Timber.w("Unable to create %s", savedReportsDir);
                    return null;
                }
            }
            return savedReportsDir;
        }

        private void moveToSharedDir(File[] savedItems, File sharedDir) {
            for (File savedItem : savedItems) {
                try {
                    FileUtils.moveDirectoryToDirectory(savedItem, sharedDir, false);
                } catch (IOException e) {
                    Timber.w(e, "Failed to move file to shared destination");
                }
            }
        }

        private void saveSharedFilesInDb(SQLiteDatabase db, File sharedDir) {
            ContentValues contentValues = new ContentValues();
            File[] savedItems = sharedDir.listFiles();

            String fileName, fileFormat;
            long creationTime;
            File savedReport;
            for (File savedItem : savedItems) {
                savedReport = new File(savedItem, savedItem.getName());
                fileName = FilenameUtils.getBaseName(savedItem.getName());
                fileFormat = FilenameUtils.getExtension(savedItem.getName()).toUpperCase(Locale.getDefault());
                creationTime = savedItem.lastModified();

                contentValues.clear();
                contentValues.put("file_path", savedReport.getPath());
                contentValues.put("name", fileName);
                contentValues.put("file_format", fileFormat);
                contentValues.put("creation_time", creationTime);
                contentValues.put("account_name", "com.jaspersoft.account.none");

                db.insert("saved_items", null, contentValues);
            }
        }
    }



}
