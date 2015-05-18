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

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
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
    public Observable<Boolean> manage() {
        return JasperAccountManager.get(mContext)
                .getAsyncActiveServerData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<AccountServerData, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final AccountServerData accountServerData) {
                        return Observable.create(new Observable.OnSubscribe<Boolean>() {
                            @Override
                            public void call(final Subscriber<? super Boolean> subscriber) {
                                final CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
                                    @Override
                                    public void onReceiveValue(Boolean value) {
                                        cookieManager.setCookie(accountServerData.getServerUrl(),
                                                accountServerData.getServerCookie());
                                        CookieManager.getInstance().flush();
                                        subscriber.onNext(value);
                                        subscriber.onCompleted();
                                    }
                                });
                            }
                        });
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Failed to sync cookies: error in obtaining token");
                    }
                });
    }

}
