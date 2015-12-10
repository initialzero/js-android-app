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

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */
public final class PasswordManager {
    static final String KEY = "PASSWORD_KEY";

    private final String mStoragePassword;
    private final Context mContext;
    private final Map<Account, Boolean> mInitializedMap = new HashMap<>();

    private PasswordManager(Context context, String storagePassword) {
        mContext = context;
        mStoragePassword = storagePassword;
    }

    public static PasswordManager create(Context context) {
        String storagePassword = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return new PasswordManager(context, storagePassword);
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
            return Hawk.init(mContext)
                    .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                    .setStorage(AccountStorage.create(mContext, account))
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
