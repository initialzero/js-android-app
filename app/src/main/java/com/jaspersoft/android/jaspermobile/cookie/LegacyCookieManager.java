/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.cookie;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LegacyCookieManager extends AbstractCookieManager {

    public LegacyCookieManager(Context context) {
        super(context);
    }

    @Override
    public void semanticConfiguration(String targetDomain) {
        CookieSyncManager.createInstance(getContext());

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        cookieManager.setCookie(targetDomain, StringUtils.join(getCookieStore(), ";"));
        CookieSyncManager.getInstance().sync();
    }

}
