/*
 * Copyright (C) 2012-2014 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.inject.Injector;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;
import com.jaspersoft.android.jaspermobile.db.database.table.ServerProfilesTable;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileProvider;
import com.jaspersoft.android.jaspermobile.webkit.WebkitCookieManagerProxy;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import java.net.CookieHandler;
import java.net.CookiePolicy;

import roboguice.RoboGuice;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class JasperMobileApplication extends Application {

    public static final String PREFS_NAME = "JasperMobileApplication.SharedPreferences";
    public static final String PREFS_CURRENT_SERVER_PROFILE_ID = "CURRENT_SERVER_PROFILE_ID";
    public static final String SAVED_REPORTS_DIR_NAME = "saved.reports";

    @Override
    public void onCreate() {
        syncCookies();
        initJsRestClient();
        populateDBIfNeed();
    }

    public static void setCurrentServerProfile(JsRestClient jsRestClient, ContentResolver contentResolver, long id) {
        String where = ServerProfilesTable._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = contentResolver.query(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                ServerProfilesTable.ALL_COLUMNS, where, selectionArgs, null);

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    ServerProfiles dbProfile = new ServerProfiles(cursor);
                    JsServerProfile serverProfile = new JsServerProfile(id, dbProfile.getAlias(),
                            dbProfile.getServerUrl(), dbProfile.getOrganization(),
                            dbProfile.getOrganization(), dbProfile.getPassword());
                    jsRestClient.setServerProfile(serverProfile);
                }
            } finally {
                cursor.close();
            }
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    /**
     * Sync cookies between HttpURLConnection and WebView
     */
    private void syncCookies() {
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieHandler.setDefault(new WebkitCookieManagerProxy(CookiePolicy.ACCEPT_ALL));
    }

    /**
     * Set timeouts and current server profile for JsRestClient instance
     */
    private void initJsRestClient() {
        // inject jsRestClient
        Injector injector = RoboGuice.getBaseApplicationInjector(this);
        JsRestClient jsRestClient = injector.getInstance(JsRestClient.class);

        // set timeouts
        int connectTimeout = SettingsActivity.getConnectTimeoutValue(this);
        int readTimeout = SettingsActivity.getReadTimeoutValue(this);
        jsRestClient.setConnectTimeout(connectTimeout * 1000);
        jsRestClient.setReadTimeout(readTimeout * 1000);

        // restore server profile id from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long profileId = prefs.getLong(PREFS_CURRENT_SERVER_PROFILE_ID, -1);

        setCurrentServerProfile(jsRestClient, getContentResolver(), profileId);
    }


    private void populateDBIfNeed() {
        Cursor cursor = getContentResolver().query(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI,
                new String[]{ServerProfilesTable._ID}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 0) {
                    ServerProfiles testProfile = new ServerProfiles();

                    testProfile.setAlias("Mobile Demo");
                    testProfile.setServerUrl("http://mobiledemo.jaspersoft.com/jasperserver-pro");
                    testProfile.setOrganization("organization_1");
                    testProfile.setUsername("phoneuser");
                    testProfile.setPassword("phoneuser");

                    getContentResolver().insert(JasperMobileProvider.SERVER_PROFILES_CONTENT_URI, testProfile.getContentValues());
                }
            } finally {
                cursor.close();
            }
        }
    }

}