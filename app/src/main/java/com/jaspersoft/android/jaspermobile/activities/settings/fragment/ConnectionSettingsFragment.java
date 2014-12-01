package com.jaspersoft.android.jaspermobile.activities.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
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
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_CONNECT_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_READ_TIMEOUT;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ConnectionSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    private JsRestClient mJsRestClient;
    @Bean
    protected DefaultPrefHelper prefHelper;

    private EditTextPreference readTimeoutPref;
    private EditTextPreference connectTimeoutPref;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
        addPreferencesFromResource(R.xml.connection_preferences);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();

        connectTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_CONNECT_TIMEOUT);
        readTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_READ_TIMEOUT);

        String value = sharedPreferences.getString(KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        String summary = getString(R.string.st_summary_sec, value);
        connectTimeoutPref.setSummary(summary);

        value = sharedPreferences.getString(KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
        summary = getString(R.string.st_summary_sec, value);
        readTimeoutPref.setSummary(summary);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

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

    private void validatePreferenceValue(String key) {
        if (key.equals(KEY_PREF_CONNECT_TIMEOUT)) {
            validatePreferenceValue(key, DEFAULT_CONNECT_TIMEOUT);
        }
        if (key.equals(KEY_PREF_READ_TIMEOUT)) {
            validatePreferenceValue(key, DEFAULT_READ_TIMEOUT);
        }
    }

    private void validatePreferenceValue(String key, String defValue) {
        if (sharedPreferences.getString(key, defValue).length() == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, defValue);
            editor.apply();
        }
    }

    private void updateDependentObjects(String key) {
        if (key.equals(KEY_PREF_CONNECT_TIMEOUT)) {
            try {
                int connectTimeOut = prefHelper.getConnectTimeoutValue();
                mJsRestClient.setConnectTimeout(connectTimeOut);
            } catch (NumberFormatException ex) {
                sharedPreferences.edit()
                        .putString(KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT)
                        .apply();
                throw ex;
            }
        }
        if (key.equals(KEY_PREF_READ_TIMEOUT)) {
            try {
                int readTimeoutValue = prefHelper.getReadTimeoutValue();
                mJsRestClient.setReadTimeout(readTimeoutValue);
            } catch (NumberFormatException ex) {
                sharedPreferences.edit()
                        .putString(KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT)
                        .apply();
                throw ex;
            }
        }
    }

    private void updatePreferenceSummary(String key) {
        if (key.equals(KEY_PREF_CONNECT_TIMEOUT)) {
            String value = sharedPreferences.getString(KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
            String summary = getString(R.string.st_summary_sec, value);
            connectTimeoutPref.setSummary(summary);
        } else if (key.equals(KEY_PREF_READ_TIMEOUT)) {
            String value = sharedPreferences.getString(KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
            String summary = getString(R.string.st_summary_sec, value);
            readTimeoutPref.setSummary(summary);
        }
    }
}
