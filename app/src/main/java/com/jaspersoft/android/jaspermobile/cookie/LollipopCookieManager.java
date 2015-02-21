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

import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class LollipopCookieManager implements JsCookieManager{
    private final Context mContext;

    public LollipopCookieManager(Context context) {
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void manage() {
        JasperAccountManager.get(mContext)
                .getActiveServerData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AccountServerData>() {
                    @Override
                    public void call(final AccountServerData serverData) {
                        final CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
                            @Override
                            public void onReceiveValue(Boolean value) {
                                cookieManager.setCookie(serverData.getServerUrl(), serverData.getServerCookie());
                                CookieManager.getInstance().flush();
                            }
                        });
                    }
                });
    }
}
