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

package com.jaspersoft.android.jaspermobile.data.cache.profile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;

/**
 * Implementation of profile cache around {@link SharedPreferences}. This cache used for persisting
 * currently active profile. Active profile - one use choose to interact across application.
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class PreferencesActiveProfileCache implements ActiveProfileCache {
    private static final String PREF_NAME = "JasperAccountManager";
    private static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME_KEY";

    private final SharedPreferences mPreference;

    @Inject
    public PreferencesActiveProfileCache(@ApplicationContext Context context) {
        mPreference = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Observable<Profile> getAsObservable() {
        return Observable.defer(new Func0<Observable<Profile>>() {
            @Override
            public Observable<Profile> call() {
                return Observable.just(get());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Profile get() {
        String key = mPreference.getString(ACCOUNT_NAME_KEY, null);
        if (key == null) {
            return null;
        }
        return Profile.create(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Profile> put(@NonNull final Profile profile) {
        return Observable.defer(new Func0<Observable<Profile>>() {
            @Override
            public Observable<Profile> call() {
                mPreference.edit().putString(ACCOUNT_NAME_KEY, profile.getKey()).apply();
                return Observable.just(profile);
            }
        });
    }

    @Override
    public boolean hasProfile() {
        return !TextUtils.isEmpty(mPreference.getString(ACCOUNT_NAME_KEY, null));
    }
}
