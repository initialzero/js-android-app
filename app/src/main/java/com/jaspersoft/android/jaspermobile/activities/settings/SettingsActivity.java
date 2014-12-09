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

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboAccentPreferenceActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.fragment.CacheSettingsFragment_;
import com.jaspersoft.android.jaspermobile.activities.settings.fragment.ConnectionSettingsFragment_;
import com.jaspersoft.android.jaspermobile.activities.settings.fragment.GeneralSettingsFragment_;
import com.negusoft.holoaccent.dialog.AccentAlertDialog;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.5
 */
@EActivity
@OptionsMenu(R.menu.settings_menu)
public class SettingsActivity extends RoboAccentPreferenceActivity {
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

    private static final String[] ALLOWED_FRAGMENTS = {
            CacheSettingsFragment_.class.getName(),
            ConnectionSettingsFragment_.class.getName(),
            GeneralSettingsFragment_.class.getName()
    };

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    /**
     * http://stackoverflow.com/questions/19973034/isvalidfragment-android-api-19
     * http://stackoverflow.com/questions/20868643/why-does-kit-kat-require-the-use-of-the-isvalidfragment
     *
     * A New Vulnerability in the Android Framework: Fragment Injection
     * We have recently disclosed a new vulnerability to the Android Security Team. The vulnerability
     * affected many apps, including Settings (the one that is found on every Android device), Gmail,
     * Google Now, DropBox and Evernote. To be more accurate, any App which extended the
     * PreferenceActivity class using an exported activity was automatically vulnerable. A patch has
     * been provided in Android KitKat. If you wondered why your code is now broken, it is due to the
     * Android KitKat patch which requires applications to override the new method,
     * PreferenceActivity.isValidFragment, which has been added to the Android Framework.
     * @param fragmentName fragment user opens
     * @return tru for older versions
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (getApplicationInfo().targetSdkVersion  >= android.os.Build.VERSION_CODES.KITKAT) {
            return Lists.newArrayList(ALLOWED_FRAGMENTS).contains(fragmentName);
        } else {
            return true;
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @OptionsItem(android.R.id.home)
    final void showHome() {
        super.onBackPressed();
    }

    @OptionsItem
    final void showAbout() {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getFragmentManager(), AboutDialog.class.getSimpleName());
    }

    //---------------------------------------------------------------------
    // Static methods
    //---------------------------------------------------------------------

    @Deprecated
    public static boolean isAnimationEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_PREF_ANIMATION_ENABLED, true);
    }

    public static class AboutDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AccentAlertDialog.Builder builder = new AccentAlertDialog.Builder(getActivity());
            builder.setTitle(R.string.sa_show_about);
            builder.setMessage(R.string.sa_about_info);
            builder.setCancelable(true);
            builder.setNeutralButton(android.R.string.ok, null);

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    View decorView = getDialog().getWindow().getDecorView();
                    if (decorView != null) {
                        TextView messageText = (TextView) decorView.findViewById(android.R.id.message);
                        if (messageText != null) {
                            messageText.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                    }
                }
            });
            return dialog;
        }
    }

}
