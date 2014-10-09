/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import roboguice.RoboGuice;

import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.DEFAULT_CONNECT_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.DEFAULT_READ_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.DEFAULT_REPO_CACHE_EXPIRATION;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_ANIMATION_ENABLED;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_CONNECT_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_READ_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_REPO_CACHE_EXPIRATION;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
    private SwitchPreference animEnabledPref;
    private EditTextPreference repoCacheExpirationPref;
    private EditTextPreference connectTimeoutPref;
    private EditTextPreference readTimeoutPref;

    @Inject
    private JsRestClient mJsRestClient;
    @Bean
    protected DefaultPrefHelper prefHelper;
    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // init shared preferences
        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        // repository cache
        repoCacheExpirationPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_REPO_CACHE_EXPIRATION);
        // timeouts
        connectTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_CONNECT_TIMEOUT);
        readTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_READ_TIMEOUT);
        animEnabledPref = (SwitchPreference) getPreferenceScreen().findPreference(KEY_PREF_ANIMATION_ENABLED);

        // init summaries for all preferences
        updatePreferenceSummary(KEY_PREF_REPO_CACHE_EXPIRATION);
        updatePreferenceSummary(KEY_PREF_CONNECT_TIMEOUT);
        updatePreferenceSummary(KEY_PREF_READ_TIMEOUT);
        updatePreferenceSummary(KEY_PREF_ANIMATION_ENABLED);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    //---------------------------------------------------------------------
    // OnSharedPreferenceChangeListener implementation
    //---------------------------------------------------------------------

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            validatePreferenceValue(key);
            updateDependentObjects(key);
            updatePreferenceSummary(key);
        } catch (NumberFormatException ex) {
            Toast.makeText(getActivity(), R.string.st_invalid_number_format, Toast.LENGTH_SHORT).show();
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void validatePreferenceValue(String key) {
        switch (key) {
            case KEY_PREF_REPO_CACHE_EXPIRATION:
                validatePreferenceValue(key, DEFAULT_REPO_CACHE_EXPIRATION);
                try {
                    prefHelper.getRepoCacheExpirationValue();
                } catch (NumberFormatException ex) {
                    sharedPreferences.edit()
                            .putString(KEY_PREF_REPO_CACHE_EXPIRATION, DEFAULT_REPO_CACHE_EXPIRATION)
                            .apply();
                    throw ex;
                }
                break;
            case KEY_PREF_CONNECT_TIMEOUT:
                validatePreferenceValue(key, DEFAULT_CONNECT_TIMEOUT);
                break;
            case KEY_PREF_READ_TIMEOUT:
                validatePreferenceValue(key, DEFAULT_READ_TIMEOUT);
                break;
        }
    }

    private void validatePreferenceValue(String key, String defValue) {
        if (sharedPreferences.getString(key, defValue).length() == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, defValue);
            editor.apply();
        }
    }

    private void updatePreferenceSummary(String key) {
        if (key.equals(KEY_PREF_REPO_CACHE_EXPIRATION)) {
            String value = sharedPreferences.getString(KEY_PREF_REPO_CACHE_EXPIRATION, DEFAULT_REPO_CACHE_EXPIRATION);
            String summary = getString(R.string.st_summary_h, value);
            repoCacheExpirationPref.setSummary(summary);
        } else if (key.equals(KEY_PREF_CONNECT_TIMEOUT)) {
            String value = sharedPreferences.getString(KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
            String summary = getString(R.string.st_summary_sec, value);
            connectTimeoutPref.setSummary(summary);
        } else if (key.equals(KEY_PREF_READ_TIMEOUT)) {
            String value = sharedPreferences.getString(KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
            String summary = getString(R.string.st_summary_sec, value);
            readTimeoutPref.setSummary(summary);
        } else if (key.equals(KEY_PREF_ANIMATION_ENABLED)) {
            boolean animationsEnabled = sharedPreferences.getBoolean(KEY_PREF_ANIMATION_ENABLED, true);
            animEnabledPref.setChecked(animationsEnabled);
        }
    }

    private void updateDependentObjects(String key) {
        switch (key) {
            case KEY_PREF_CONNECT_TIMEOUT:
                try {
                    int readTimeoutValue = prefHelper.getConnectTimeoutValue();
                    mJsRestClient.setConnectTimeout(readTimeoutValue * 1000);
                } catch (NumberFormatException ex) {
                    sharedPreferences.edit()
                            .putString(KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT)
                            .apply();
                    throw ex;
                }
                break;
            case KEY_PREF_READ_TIMEOUT:
                try {
                    int readTimeoutValue = prefHelper.getReadTimeoutValue();
                    mJsRestClient.setReadTimeout(readTimeoutValue * 1000);
                } catch (NumberFormatException ex) {
                    sharedPreferences.edit()
                            .putString(KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT)
                            .apply();
                    throw ex;
                }
                break;
        }
    }

}
