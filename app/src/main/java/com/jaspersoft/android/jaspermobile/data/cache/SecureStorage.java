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

package com.jaspersoft.android.jaspermobile.data.cache;

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.observers.Observers;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */
@Singleton
public final class SecureStorage implements SecureCache {

    private boolean isInitialized = false;
    private final ConnectableObservable<Boolean> initObservable;

    @Inject
    public SecureStorage(@ApplicationContext Context context,
                         PreExecutionThread preExecutionThread,
                         PostExecutionThread postExecutionThread) {
        String storagePassword = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        initObservable = Hawk.init(context)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                .setPassword(storagePassword)
                .buildRx()
                .subscribeOn(preExecutionThread.getScheduler())
                .observeOn(postExecutionThread.getScheduler())
                .publish();

        initObservable.subscribe(Observers.create(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                isInitialized = aBoolean;
            }
        }));
        initObservable.connect();
    }

    @Override
    public void put(final String key, final String rawValue) {
        if (isInitialized) {
            Hawk.put(key, rawValue);
        } else {
            initObservable.subscribe(Observers.create(new Action1<Boolean>() {
                @Override
                public void call(Boolean initialized) {
                    if (initialized) {
                        Hawk.put(key, rawValue);
                    }
                }
            }));
        }
    }

    @Nullable
    @Override
    public String get(final String key) {
        if (isInitialized) {
            return Hawk.get(key);
        } else {
            Boolean initialized = initObservable.toBlocking().firstOrDefault(false);
            if (initialized) {
                return Hawk.get(key);
            }
            return null;
        }
    }
}
