package com.jaspersoft.android.jaspermobile.activities.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import roboguice.RoboGuice;

import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.DEFAULT_REPO_CACHE_EXPIRATION;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_REPO_CACHE_EXPIRATION;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class CacheSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;
    private EditTextPreference repoCacheExpirationPref;

    @Bean
    protected DefaultPrefHelper prefHelper;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
        addPreferencesFromResource(R.xml.cache_preferences);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        repoCacheExpirationPref = (EditTextPreference) getPreferenceScreen()
                .findPreference(KEY_PREF_REPO_CACHE_EXPIRATION);

        String value = sharedPreferences.getString(KEY_PREF_REPO_CACHE_EXPIRATION,
                DEFAULT_REPO_CACHE_EXPIRATION);
        String summary = getString(R.string.st_summary_h, value);
        repoCacheExpirationPref.setSummary(summary);
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
        validatePreferenceValue(key, DEFAULT_REPO_CACHE_EXPIRATION);
        try {
            prefHelper.getRepoCacheExpirationValue();
        } catch (NumberFormatException ex) {
            sharedPreferences.edit()
                    .putString(KEY_PREF_REPO_CACHE_EXPIRATION, DEFAULT_REPO_CACHE_EXPIRATION)
                    .apply();
            Toast.makeText(getActivity(),
                    R.string.st_invalid_number_format, Toast.LENGTH_SHORT).show();
        }
        String value = sharedPreferences.getString(KEY_PREF_REPO_CACHE_EXPIRATION,
                DEFAULT_REPO_CACHE_EXPIRATION);
        String summary = getString(R.string.st_summary_h, value);
        repoCacheExpirationPref.setSummary(summary);
    }

    private void validatePreferenceValue(String key, String defValue) {
        if (sharedPreferences.getString(key, defValue).length() == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, defValue);
            editor.apply();
        }
    }

}
