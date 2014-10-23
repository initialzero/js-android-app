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

package com.jaspersoft.android.jaspermobile.network;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.splunk.mint.Mint;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class BugSenseWrapper {

    private static final String API_KEY = "b1a1b0d2";

    public static void initAndStartSession(Context context) {
        if (sendReport(context)) {
            Mint.initAndStartSession(context, API_KEY);
        }
    }

    public static void startSession(Context context) {
        if (sendReport(context)) {
            Mint.startSession(context);
        }
    }

    public static void closeSession(Context context) {
        Mint.closeSession(context);
    }

    private static boolean sendReport(Context context) {
        DefaultPrefHelper_ prefHelper = DefaultPrefHelper_.getInstance_(context);
        return (!BuildConfig.DEBUG && prefHelper.sendCrashReports());
    }

}

