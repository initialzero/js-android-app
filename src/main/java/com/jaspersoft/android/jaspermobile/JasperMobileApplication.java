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
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.jaspersoft.android.jaspermobile.webkit.WebkitCookieManagerProxy;

import java.net.CookieHandler;
import java.net.CookiePolicy;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
public class JasperMobileApplication extends Application {

    public static final String SAVED_REPORTS_DIR_NAME = "saved.reports";

    @Override
    public void onCreate() {
        // Sync cookies between HttpURLConnection and WebView
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieHandler.setDefault(new WebkitCookieManagerProxy(CookiePolicy.ACCEPT_ALL));
    }

}