package com.jaspersoft.android.jaspermobile.activities.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.TwoStatePreference;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.intro.IntroPageActivity_;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.EFragment;

import roboguice.fragment.provided.RoboPreferenceFragment;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EFragment
public class SettingsFragment extends RoboPreferenceFragment {

    @Inject
    private JsRestClient mJsRestClient;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        EditTextPreference repoCacheExpirationPref = (EditTextPreference) getPreferenceScreen().findPreference(DefaultPrefHelper.KEY_PREF_REPO_CACHE_EXPIRATION);
        EditTextPreference connectTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(DefaultPrefHelper.KEY_PREF_CONNECT_TIMEOUT);
        EditTextPreference readTimeoutPref = (EditTextPreference) getPreferenceScreen().findPreference(DefaultPrefHelper.KEY_PREF_READ_TIMEOUT);
        TwoStatePreference sendCrashesPref = (TwoStatePreference) getPreferenceScreen().findPreference(DefaultPrefHelper.KEY_PREF_SEND_CRASHES);

        String repoCacheExpiration = sharedPreferences.getString(DefaultPrefHelper.KEY_PREF_REPO_CACHE_EXPIRATION, DefaultPrefHelper.DEFAULT_REPO_CACHE_EXPIRATION);
        String connectTimeout = sharedPreferences.getString(DefaultPrefHelper.KEY_PREF_CONNECT_TIMEOUT, DefaultPrefHelper.DEFAULT_CONNECT_TIMEOUT);
        String readTimeout = sharedPreferences.getString(DefaultPrefHelper.KEY_PREF_READ_TIMEOUT, DefaultPrefHelper.DEFAULT_READ_TIMEOUT);

        updateSummary(repoCacheExpirationPref, repoCacheExpiration, R.string.st_summary_h);
        updateSummary(connectTimeoutPref, connectTimeout, R.string.st_summary_sec);
        updateSummary(readTimeoutPref, readTimeout, R.string.st_summary_sec);

        repoCacheExpirationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateSummary(preference, newValue, R.string.st_summary_h);
                return true;
            }
        });

        connectTimeoutPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateSummary(preference, newValue, R.string.st_summary_sec);
                mJsRestClient.setConnectTimeout(Integer.parseInt(String.valueOf(newValue)));
                return true;
            }
        });

        readTimeoutPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateSummary(preference, newValue, R.string.st_summary_sec);
                mJsRestClient.setReadTimeout(Integer.parseInt(String.valueOf(newValue)));
                return true;
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

        Preference showIntro = getPreferenceScreen().findPreference(DefaultPrefHelper.KEY_PREF_SHOW_INTRO);
        showIntro.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                IntroPageActivity_.intent(getActivity()).start();
                return true;
            }
        });
    }

    private void updateSummary(Preference preference, Object value, int summaryDefaultText) {
        String summary = getString(summaryDefaultText, value);
        preference.setSummary(summary);
    }

}
