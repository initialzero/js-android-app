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

package com.jaspersoft.android.jaspermobile.util.security;

import android.accounts.Account;
import android.content.Context;
import android.provider.Settings;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */

@EBean(scope = EBean.Scope.Singleton)
public class PasswordManager {
    @RootContext
    protected Context context;

    static final String KEY = "PASSWORD_KEY";

    private String mStoragePassword;
    private final Map<Account, Boolean> mInitializedMap = new HashMap<>();

    @AfterInject
    protected void init(){
        mStoragePassword = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public Observable<Boolean> put(Account account, final String plainPassword) {
        Observable<Boolean> initOperation = initHawk(account);
        return initOperation.flatMap(new Func1<Boolean, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Boolean aBoolean) {
                return Hawk.putObservable(KEY, plainPassword);
            }
        }).onErrorReturn(new Func1<Throwable, Boolean>() {
            @Override
            public Boolean call(Throwable throwable) {
                return false;
            }
        });
    }

    public Observable<String> get(Account account) {
        Observable<Boolean> initOperation = initHawk(account);
        return initOperation.flatMap(new Func1<Boolean, Observable<String>>() {
            @Override
            public Observable<String> call(Boolean aBoolean) {
                return Hawk.getObservable(KEY);
            }
        }).onErrorReturn(new Func1<Throwable, String>() {
            @Override
            public String call(Throwable throwable) {
                return null;
            }
        });
    }

    private Observable<Boolean> initHawk(final Account account) {
        if (!mInitializedMap.containsKey(account)) {
            return Hawk.init(context)
                    .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                    .setStorage(AccountStorage.create(context, account))
                    .setPassword(mStoragePassword)
                    .buildRx()
                    .doOnNext(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean isInitialized) {
                            mInitializedMap.put(account, isInitialized);
                        }
                    });
        }
        return Observable.just(mInitializedMap.get(account));
    }
}
