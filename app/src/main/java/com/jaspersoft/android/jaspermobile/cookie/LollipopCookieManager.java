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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LollipopCookieManager implements JsCookieManager {
    private final Context mContext;

    public LollipopCookieManager(Context context) {
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void manage() {
        AccountServerData serverData;
        try {
            serverData = JasperAccountManager.get(mContext).getActiveServerData();
            final CookieManager cookieManager = CookieManager.getInstance();
            final AccountServerData finalServerData = serverData;
            cookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    cookieManager.setCookie(finalServerData.getServerUrl(), finalServerData.getServerCookie());
                    CookieManager.getInstance().flush();
                }
            });
        } catch (JasperAccountManager.TokenException e) {
            Timber.e(e, "Failed to sync cookies: error in obtaining token");
        }
    }
}
