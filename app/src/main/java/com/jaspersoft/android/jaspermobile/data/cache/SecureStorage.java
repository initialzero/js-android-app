/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.cache;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.AccountDataMapper;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.util.account.AccountStorage;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.Storage;

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

    private final HawkBuilder hawkBuilder;
    private final AccountManager mAccountManager;
    private final AccountDataMapper mAccountDataMapper;
    private final PreExecutionThread mPreExecutionThread;
    private final PostExecutionThread mPostExecutionThread;

    private boolean isInitialized = false;
    private ConnectableObservable<Boolean> initObservable;
    private Profile mCurrentProfile;

    @Inject
    public SecureStorage(@ApplicationContext Context context,
                         AccountDataMapper accountDataMapper,
                         PreExecutionThread preExecutionThread,
                         PostExecutionThread postExecutionThread
    ) {
        mAccountManager = AccountManager.get(context);
        mAccountDataMapper = accountDataMapper;
        mPreExecutionThread = preExecutionThread;
        mPostExecutionThread = postExecutionThread;

        String storagePassword = Settings.Secure.getString(
                context.getContentResolver(),Settings.Secure.ANDROID_ID);
        hawkBuilder = Hawk.init(context)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
                .setPassword(storagePassword);
    }


    @Override
    public void put(Profile profile, final String key, final String rawValue) {
        if (profileChanged(profile)) {
            setupProfileStorage(profile);
        }

        if (isInitialized) {
            Hawk.put(key, rawValue);
        } else {
            Boolean initialized = initObservable.toBlocking().firstOrDefault(false);
            if (initialized) {
                Hawk.put(key, rawValue);
            }
        }
    }

    @Nullable
    @Override
    public String get(Profile profile, String key) {
        if (profileChanged(profile)) {
            setupProfileStorage(profile);
        }

        try {
            return getFromHawk(key);
        } catch (IllegalArgumentException ex) {
            // We are catching error: "Text should contain delimiter"
            // This happens during migration from 2.2.1 to 2.2.2
            return null;
        }
    }

    @Nullable
    private String getFromHawk(String key) {
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

    private boolean profileChanged(Profile profile) {
        return !profile.equals(mCurrentProfile);
    }

    private void setupProfileStorage(Profile profile) {
        Storage storage = createStorage(profile);
        initHawk(storage);
    }

    private Storage createStorage(Profile profile) {
        mCurrentProfile = profile;

        Account account = mAccountDataMapper.transform(profile);
        return new AccountStorage(mAccountManager, account);
    }

    private void initHawk(Storage storage) {
        isInitialized = false;
        initObservable = hawkBuilder
                .setStorage(storage)
                .buildRx()
                .subscribeOn(mPreExecutionThread.getScheduler())
                .observeOn(mPostExecutionThread.getScheduler())
                .publish();

        initObservable.subscribe(Observers.create(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                isInitialized = aBoolean;
            }
        }));
        initObservable.connect();
    }
}
