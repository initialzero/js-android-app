/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.concurrent.TimeUnit;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean(scope = EBean.Scope.Singleton)
public class DefaultPrefHelper {
    @RootContext
    Context context;

    public int getConnectTimeoutValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(
                SettingsActivity.KEY_PREF_CONNECT_TIMEOUT, SettingsActivity.DEFAULT_CONNECT_TIMEOUT);
        return (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(value));
    }

    public int getReadTimeoutValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(
                SettingsActivity.KEY_PREF_READ_TIMEOUT, SettingsActivity.DEFAULT_READ_TIMEOUT);
        return (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(value));
    }

    public boolean isAnimationEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(SettingsActivity.KEY_PREF_ANIMATION_ENABLED, true);
    }

    public void setAnimationEnabled(boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(SettingsActivity.KEY_PREF_ANIMATION_ENABLED, value).apply();
    }

    public boolean sendCrashReports() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(SettingsActivity.KEY_PREF_SEND_CRASHES, true);
    }

    public void setRepoCacheEnabled(boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(
                SettingsActivity.KEY_PREF_REPO_CACHE_ENABLED, value).apply();
    }

    public long getRepoCacheExpirationValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean repoCacheEnabled = preferences.getBoolean(
                SettingsActivity.KEY_PREF_REPO_CACHE_ENABLED, SettingsActivity.DEFAULT_REPO_CACHE_ENABLED);

        if (repoCacheEnabled) {
            String value = preferences.getString(
                    SettingsActivity.KEY_PREF_REPO_CACHE_EXPIRATION, SettingsActivity.DEFAULT_REPO_CACHE_EXPIRATION);
            return Integer.parseInt(value) * DurationInMillis.ONE_HOUR;
        } else {
            return -1;
        }
    }
}
