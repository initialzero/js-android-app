package com.jaspersoft.android.jaspermobile.activities.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.TwoStatePreference;
import android.widget.Toast;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import roboguice.fragment.provided.RoboPreferenceFragment;

import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.DEFAULT_CONNECT_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.DEFAULT_READ_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.DEFAULT_REPO_CACHE_EXPIRATION;
import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.KEY_PREF_CONNECT_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.KEY_PREF_READ_TIMEOUT;
import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.KEY_PREF_REPO_CACHE_EXPIRATION;
import static com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper.KEY_PREF_SEND_CRASHES;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EFragment
public class SettingsFragment extends RoboPreferenceFragment {

    @Inject
    private JsRestClient mJsRestClient;
    @Bean
    protected DefaultPrefHelper defaultPrefHelper;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        EditTextPreference repoCacheExpirationPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_REPO_CACHE_EXPIRATION);
        EditTextPreference connectTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_CONNECT_TIMEOUT);
        EditTextPreference readTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(KEY_PREF_READ_TIMEOUT);
        TwoStatePreference sendCrashesPref = (TwoStatePreference) getPreferenceScreen().findPreference(KEY_PREF_SEND_CRASHES);

        String repoCacheExpiration = sharedPreferences.getString(KEY_PREF_REPO_CACHE_EXPIRATION, DEFAULT_REPO_CACHE_EXPIRATION);
        String connectTimeout = sharedPreferences.getString(KEY_PREF_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        String readTimeout = sharedPreferences.getString(KEY_PREF_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);

        updateSummary(repoCacheExpirationPref, repoCacheExpiration, R.string.st_summary_h);
        updateSummary(connectTimeoutPref, connectTimeout, R.string.st_summary_sec);
        updateSummary(readTimeoutPref, readTimeout, R.string.st_summary_sec);

        repoCacheExpirationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    newValue = Integer.parseInt(String.valueOf(newValue));
                    updateSummary(preference, newValue, R.string.st_summary_h);
                    return true;
                } catch (NumberFormatException ex) {
                    Toast.makeText(getActivity(),
                            R.string.st_invalid_number_format, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        connectTimeoutPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int value = Integer.parseInt(String.valueOf(newValue));
                    mJsRestClient.setConnectTimeout(value);
                    updateSummary(preference, newValue, R.string.st_summary_sec);
                    return true;
                } catch (NumberFormatException ex) {
                    Toast.makeText(getActivity(),
                            R.string.st_invalid_number_format, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        readTimeoutPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int value = Integer.parseInt(String.valueOf(newValue));
                    mJsRestClient.setReadTimeout(value);
                    updateSummary(preference, newValue, R.string.st_summary_sec);
                    return true;
                } catch (NumberFormatException ex) {
                    Toast.makeText(getActivity(),
                            R.string.st_invalid_number_format, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        sendCrashesPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!(boolean) newValue)
                    BugSenseWrapper.closeSession(getActivity());
                return true;
            }
        });
    }

    private void updateSummary(Preference preference, Object value, int summaryDefaultText) {
        String summary = getString(summaryDefaultText, value);
        preference.setSummary(summary);
    }

}
