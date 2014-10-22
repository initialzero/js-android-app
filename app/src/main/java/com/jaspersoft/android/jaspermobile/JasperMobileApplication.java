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

package com.jaspersoft.android.jaspermobile;

import android.app.Application;
import android.view.ViewConfiguration;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.jaspermobile.webkit.WebkitCookieManagerProxy;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import java.lang.reflect.Field;
import java.net.CookieHandler;
import java.net.CookiePolicy;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
@EApplication
public class JasperMobileApplication extends Application {
    private static final String API_KEY = "b1a1b0d2";
    public static final String SAVED_REPORTS_DIR_NAME = "saved.reports";

    @Bean
    ProfileHelper profileHelper;

    @Override
    public void onCreate() {
        forceOverFlowMenu();
        syncCookies();
        profileHelper.initJsRestClient();
        profileHelper.seedProfilesIfNeed();
        BugSenseWrapper.initAndStartSession(this, API_KEY);
    }

    /**
     * We are forcing OS to show overflow menu for the devices which expose hardware implementation.
     * WARNING: This is considered to be bad practice though we decide to violate rules.
     * http://stackoverflow.com/questions/9286822/how-to-force-use-of-overflow-menu-on-devices-with-menu-button
     */
    private void forceOverFlowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
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

}