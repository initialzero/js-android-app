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

package com.jaspersoft.android.jaspermobile.cookie;

import android.content.Context;
import android.os.Build;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class CookieManagerFactory {

    /**
     * Sync cookies between HttpURLConnection and WebView
     *
     * @param context required for initialization of {@link android.webkit.CookieSyncManager} instamce
     */
    public static Observable<Boolean> syncCookies(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }
        return CookieManagerFactory.createManager(context).manage();
    }

    /**
     * Creates implementation of manger on the basis of current SDK version.
     */
    private static JsCookieManager createManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new LollipopCookieManager(context);
        } else {
            return new LegacyCookieManager(context);
        }
    }

}
