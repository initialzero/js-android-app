package com.jaspersoft.android.jaspermobile.activities.settings.fragment;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity;
import com.jaspersoft.android.jaspermobile.activities.HomeActivity_;
import com.jaspersoft.android.jaspermobile.activities.intro.IntroPageActivity_;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;

import roboguice.RoboGuice;

import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_ANIMATION_ENABLED;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_SEND_CRASHES;
import static com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity.KEY_PREF_SHOW_INTRO;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class GeneralSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SystemService
    protected AlarmManager alarmManager;

    private SwitchPreference animEnabledPref;
    private SwitchPreference sendCrashesPref;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
        addPreferencesFromResource(R.xml.general_preferences);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();

        animEnabledPref = (SwitchPreference) getPreferenceScreen().findPreference(KEY_PREF_ANIMATION_ENABLED);
        sendCrashesPref = (SwitchPreference) getPreferenceScreen().findPreference(KEY_PREF_SEND_CRASHES);
        Preference showIntro = getPreferenceScreen().findPreference(KEY_PREF_SHOW_INTRO);
        showIntro.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                IntroPageActivity_.intent(getActivity()).start();
                return true;
            }
        });
        boolean animationsEnabled = sharedPreferences.getBoolean(KEY_PREF_ANIMATION_ENABLED, true);
        animEnabledPref.setChecked(animationsEnabled);
        boolean sendCrashReports = sharedPreferences.getBoolean(KEY_PREF_SEND_CRASHES, true);
        sendCrashesPref.setChecked(sendCrashReports);
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
        updatePreferenceSummary(key);
        updateDependentObjects(key);
    }

    private void updatePreferenceSummary(String key) {
        if (key.equals(KEY_PREF_ANIMATION_ENABLED)) {
            boolean animationsEnabled = sharedPreferences.getBoolean(KEY_PREF_ANIMATION_ENABLED, true);
            animEnabledPref.setChecked(animationsEnabled);
        } else if (key.equals(KEY_PREF_SEND_CRASHES)) {
            boolean sendCrashReports = sharedPreferences.getBoolean(KEY_PREF_SEND_CRASHES, true);
            sendCrashesPref.setChecked(sendCrashReports);
        }
    }

    private void updateDependentObjects(String key) {
        if (key.equals(KEY_PREF_SEND_CRASHES)) {
            if (!sendCrashesPref.isChecked())
                BugSenseWrapper.closeSession(getActivity());
            HomeActivity_.intent(getActivity())
                    .flags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .action(HomeActivity.CLOSE_APPLICATION_ACTION)
                    .start();
        }
    }

}
