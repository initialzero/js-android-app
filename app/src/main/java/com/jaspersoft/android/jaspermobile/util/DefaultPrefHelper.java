/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

import com.jaspersoft.android.jaspermobile.dialog.RateAppDialog;
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
    public static final String KEY_PREF_REPO_CACHE_ENABLED = "pref_repo_cache_enabled";
    public static final String KEY_PREF_REPO_CACHE_EXPIRATION = "pref_repo_cache_expiration";
    public static final String KEY_PREF_CONNECT_TIMEOUT = "pref_connect_timeout";
    public static final String KEY_PREF_READ_TIMEOUT = "pref_read_timeout";
    public static final String KEY_PREF_ANIMATION_ENABLED = "pref_animation_enabled";
    public static final String KEY_PREF_SEND_CRASHES = "pref_crash_reports";

    public static final boolean DEFAULT_REPO_CACHE_ENABLED = true;
    public static final String DEFAULT_REPO_CACHE_EXPIRATION = "48";
    public static final String DEFAULT_CONNECT_TIMEOUT = "15";
    public static final String DEFAULT_READ_TIMEOUT = "120";

    @RootContext
    Context context;

    public int getConnectTimeoutValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(
                KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        return (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(value));
    }

    public int getReadTimeoutValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(
                KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
        return (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(value));
    }

    public boolean isAnimationEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_PREF_ANIMATION_ENABLED, true);
    }

    public void setAnimationEnabled(boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(KEY_PREF_ANIMATION_ENABLED, value).apply();
    }

    public boolean sendCrashReports() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_PREF_SEND_CRASHES, true);
    }

    public void setRepoCacheEnabled(boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(KEY_PREF_REPO_CACHE_ENABLED, value).apply();
    }

    public long getRepoCacheExpirationValue() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean repoCacheEnabled = preferences.getBoolean(KEY_PREF_REPO_CACHE_ENABLED, DEFAULT_REPO_CACHE_ENABLED);

        if (repoCacheEnabled) {
            String value = preferences.getString(KEY_PREF_REPO_CACHE_EXPIRATION, DEFAULT_REPO_CACHE_EXPIRATION);
            return Integer.parseInt(value) * DurationInMillis.ONE_HOUR;
        } else {
            return -1;
        }
    }

    public boolean isRateDialogEnabled(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(RateAppDialog.KEY_PREF_NEED_TO_RATE, true);
    }

    public void setRateDialogEnabled(boolean value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(
                RateAppDialog.KEY_PREF_NEED_TO_RATE, value).apply();
    }

    public long getLastRateTime(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(RateAppDialog.KEY_PREF_LAST_RATE_TIME, 0);
    }

    public void setLastRateTime(long value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(
                RateAppDialog.KEY_PREF_LAST_RATE_TIME, value).apply();
    }

    public long getNonRateLaunchCount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(RateAppDialog.KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE, 0);
    }

    public void increaseNonRateLaunchCount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long nonRateLaunchCount = getNonRateLaunchCount();
        nonRateLaunchCount = nonRateLaunchCount == RateAppDialog.LAUNCHES_UNTIL_SHOW ? nonRateLaunchCount : nonRateLaunchCount + 1;
        preferences.edit().putLong(
                RateAppDialog.KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE, nonRateLaunchCount).apply();
    }

    public void resetNonRateLaunchCount(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(
                RateAppDialog.KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE, 0).apply();
    }
}
