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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.inject.Injector;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.db.tables.ServerProfiles;
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
    }

    public static void setCurrentServerProfile(JsRestClient jsRestClient, DatabaseProvider dbProvider, long profileId) {
        // Get a cursor with server profile
        Cursor cursor = dbProvider.fetchServerProfile(profileId);
        // check if the server profile exists in db
        if (cursor.getCount() != 0) {
            // Retrieve the column indexes for that particular server profile
            int aliasId = cursor.getColumnIndex(ServerProfiles.KEY_ALIAS);
            int urlId = cursor.getColumnIndex(ServerProfiles.KEY_SERVER_URL);
            int orgId = cursor.getColumnIndex(ServerProfiles.KEY_ORGANIZATION);
            int usrId = cursor.getColumnIndex(ServerProfiles.KEY_USERNAME);
            int pwdId = cursor.getColumnIndex(ServerProfiles.KEY_PASSWORD);
            // create new profile from cursor
            JsServerProfile serverProfile = new JsServerProfile(profileId, cursor.getString(aliasId),
                    cursor.getString(urlId), cursor.getString(orgId), cursor.getString(usrId), cursor.getString(pwdId));
            jsRestClient.setServerProfile(serverProfile);
        }
        // release resources
        cursor.close();
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

        // Get the database provider
        DatabaseProvider dbProvider = new DatabaseProvider(this);

        // restore server profile id from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long profileId = prefs.getLong(PREFS_CURRENT_SERVER_PROFILE_ID, -1);

        setCurrentServerProfile(jsRestClient, dbProvider, profileId);

        dbProvider.close();
    }

}