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

package com.jaspersoft.android.jaspermobile.data.repository.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;

import com.jaspersoft.android.jaspermobile.data.cache.profile.AccountProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.PreferencesActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;
import com.jaspersoft.android.jaspermobile.internal.di.modules.app.CacheModule;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 * Implementation of repo pattern for {@link Profile}
 *
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class ProfileDataRepository implements ProfileRepository {

    private final AccountManager mAccountManager;
    /**
     * Injected by {@link CacheModule#providesProfileAccountCache(AccountProfileCache)}}
     */
    private final ProfileCache mAccountCache;
    /**
     * Injected by {@link CacheModule#providesPreferencesProfileCache(PreferencesActiveProfileCache)}}
     */
    private final ActiveProfileCache mPrefActiveCache;

    @Inject
    public ProfileDataRepository(
            AccountManager accountManager,
            ProfileCache accountProfileCache,
            ActiveProfileCache preferencesProfileCache
    ) {
        mAccountManager = accountManager;
        mAccountCache = accountProfileCache;
        mPrefActiveCache = preferencesProfileCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile saveProfile(final Profile profile) {
        mAccountCache.put(profile);
        return profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile activate(final Profile profile) {
        mPrefActiveCache.put(profile);
        return profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<List<Profile>> listProfiles() {
        return Observable.create(new Observable.OnSubscribe<List<Profile>>() {
            @Override
            public void call(final Subscriber<? super List<Profile>> subscriber) {
                final OnAccountsUpdateListener listener = new OnAccountsUpdateListener() {
                    @Override
                    public void onAccountsUpdated(Account[] accounts) {
                        try {
                            List<Profile> profiles = mAccountCache.getAll();
                            subscriber.onNext(profiles);
                        } catch (Exception ex) {
                            subscriber.onError(ex);
                        }
                    }
                };

                mAccountManager.addOnAccountsUpdatedListener(listener, null, true);

                subscriber.add(new MainThreadSubscription() {
                    @Override protected void onUnsubscribe() {
                        mAccountManager.removeOnAccountsUpdatedListener(listener);
                    }
                });
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile getActiveProfile() {
        return mPrefActiveCache.get();
    }
}
