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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.jaspersoft.android.jaspermobile.domain.Profile;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class PreferencesProfileCache implements ProfileCache {
    private static final String PREF_NAME = "JasperAccountManager";
    private static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME_KEY";

    private final SharedPreferences mPreference;

    @Inject
    public PreferencesProfileCache(Context context) {
        mPreference = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }

    @Override
    public Profile get() {
        String key = mPreference.getString(ACCOUNT_NAME_KEY, null);
        if (key == null) {
            return null;
        }
        return Profile.create(key);
    }

    @Override
    public boolean put(Profile profile) {
        mPreference.edit().putString(ACCOUNT_NAME_KEY, profile.getKey()).apply();
        return true;
    }

    @Override
    public boolean hasProfile(Profile profile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void evict() {
        mPreference.edit().remove(ACCOUNT_NAME_KEY).apply();
    }
}
