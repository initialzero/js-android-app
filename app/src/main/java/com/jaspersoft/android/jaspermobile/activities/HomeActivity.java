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

package com.jaspersoft.android.jaspermobile.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesActivity_;
import com.jaspersoft.android.jaspermobile.activities.intro.IntroPageActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.ServerProfileActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.ServersManagerActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.fragment.ServersFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryActivity_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsActivity_;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.PasswordDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ConnectivityUtil;
import com.jaspersoft.android.jaspermobile.util.GeneralPref_;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @since 1.0
 */
@EActivity(R.layout.home_layout)
@OptionsMenu(R.menu.home_menu)
public class HomeActivity extends RoboSpiceFragmentActivity {
    private static final int PENDING_INTENT_ID = 123456;

    // Special intent actions
    public static final String EDIT_SERVER_PROFILE_ACTION = "com.jaspersoft.android.jaspermobile.action.EDIT_SERVER_PROFILE";
    public static final String CLOSE_APPLICATION_ACTION = "com.jaspersoft.android.samples.jaspermobile.action.CLOSE_APPLICATION";
    // Request Codes
    public static final int RC_UPDATE_SERVER_PROFILE = 20;
    public static final int RC_SWITCH_SERVER_PROFILE = 21;

    @Inject
    private JsRestClient mJsRestClient;
    @Inject
    private ConnectivityUtil mConnectivityUtil;
    @Inject
    private DecelerateInterpolator interpolator;
    @Inject
    @Named("animationSpeed")
    private int mAnimationSpeed;

    @ViewById
    protected ViewGroup table;
    @OptionsMenuItem
    protected MenuItem serverProfileMenuItem;
    @InstanceState
    protected boolean mAnimateStartup = true;

    @Pref
    GeneralPref_ generalPref;
    @Bean
    ProfileHelper profileHelper;

    private TextView mProfileNameText;
    private Bundle mSavedInstanceState;

    private final Handler mHandler = new Handler();
    private final Runnable restartAppTask = new Runnable() {
        @Override
        public void run() {
            Activity context = HomeActivity.this;
            Intent mStartActivity = HomeActivity_.intent(context).get();
            PendingIntent mPendingIntent = PendingIntent.getActivity(context,
                    PENDING_INTENT_ID, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        }
    };

    //---------------------------------------------------------------------
    // Static methods
    //---------------------------------------------------------------------

    public static void goHome(Context context) {
        // All of the other activities on top of it will be destroyed and this intent will be delivered
        // to the resumed instance of the activity (now on top), through onNewIntent()
        HomeActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
    }

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        View actionView = serverProfileMenuItem.getActionView();
        mProfileNameText = (TextView) actionView.findViewById(R.id.profile_name);
        reloadProfileNameView();
        return result;
    }

    //---------------------------------------------------------------------
    // Annotated methods
    //---------------------------------------------------------------------

    @AfterViews
    final void init() {
        if (mAnimateStartup) {
            mAnimateStartup = false;
            animateLayout();
        }
    }

    @Click(R.id.home_item_repository)
    final void showRepository() {
        if (hasNetwork() && hasServerProfileSetup()) {
            RepositoryActivity_.intent(this).start();
        }
    }

    @Click(R.id.home_item_library)
    final void showLibrary() {
        if (hasNetwork() && hasServerProfileSetup()) {
            LibraryActivity_.intent(this).start();
        }
    }

    @Click(R.id.home_item_favorites)
    final void showFavorites() {
        if (hasNetwork() && hasServerProfileSetup()) {
            FavoritesActivity_.intent(this).start();
        }
    }

    @Click(R.id.home_item_saved_reports)
    final void showSavedItems() {
        SavedReportsActivity_.intent(this).start();
    }

    @Click(R.id.home_item_settings)
    final void showSettings() {
        SettingsActivity_.intent(this).start();
    }

    @Click(R.id.home_item_servers)
    final void showServerProfiles() {
        ServersManagerActivity_.intent(this).startForResult(RC_SWITCH_SERVER_PROFILE);
    }

    @OnActivityResult(RC_UPDATE_SERVER_PROFILE)
    final void updateSeverProfile(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // get updated server profile id from result data
            Bundle extras = data.getExtras();
            long profileId = extras.getLong(ServersFragment.EXTRA_SERVER_PROFILE_ID);

            // update current profile
            profileHelper.setCurrentServerProfile(profileId);
            JsServerProfile profile = mJsRestClient.getServerProfile();
            // check if the password is not specified
            if (profile.getPassword().length() == 0) {
                PasswordDialogFragment.show(getSupportFragmentManager());
            }
        }
    }

    @OnActivityResult(RC_SWITCH_SERVER_PROFILE)
    final void switchServerProfile(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // get selected server profile id from result data
            Bundle extras = data.getExtras();
            long profileId = extras.getLong(ServersFragment.EXTRA_SERVER_PROFILE_ID);

            generalPref.currentProfileId().put(profileId);
            profileHelper.setCurrentServerProfile(profileId);
        }

        // This inconsistent state possible for the situation when user ignores profile
        // either selection(or creation and selection) step
        boolean profileSelected = mJsRestClient.getServerProfile() != null;
        if (profileSelected) {
            // check if the password is not specified
            if (mJsRestClient.getServerProfile().getPassword().length() == 0) {
                PasswordDialogFragment.show(getSupportFragmentManager());
            }

            invalidateOptionsMenu();

            // the feedback about an operation
            String profileName = mJsRestClient.getServerProfile().getAlias();
            String toastMsg = getString(R.string.h_server_switched_toast, profileName);
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
        }
    }

    //---------------------------------------------------------------------
    // Protected methods
    //---------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        IntroPageActivity_.intent(this).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (EDIT_SERVER_PROFILE_ACTION.equals(intent.getAction())) {
            // Launch activity to edit current server profile
            ServerProfileActivity_.intent(this)
                    .inEditMode(true)
                    .profileId(mJsRestClient.getServerProfile().getId())
                    .startForResult(RC_UPDATE_SERVER_PROFILE);
            return;
        }

        if (CLOSE_APPLICATION_ACTION.equals(intent.getAction())) {
            mHandler.postDelayed(restartAppTask, 100);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void animateLayout() {
        boolean animationEnabled = SettingsActivity.isAnimationEnabled(this);
        // No sense in animating if no speed set up
        // '0' case possible while black box testing
        if (animationEnabled && mAnimationSpeed > 0) {
            int childCount = table.getChildCount();
            int defaultDelay = 200;
            for (int i = 0; i < childCount; i++) {
                View child = table.getChildAt(i);
                animateRow(child, defaultDelay);
                defaultDelay += 100;
            }
        }
    }

    private void animateRow(final View view, int delay) {
        if (view == null) return;
        view.setTranslationX(0.0F);
        view.setAlpha(0);
        view.setRotationX(45.0F);
        view.setScaleX(0.7F);
        view.setScaleY(0.55F);
        view.animate()
                .rotationX(0.0F).rotationY(0.0F)
                .translationX(0).translationY(0)
                .scaleX(1.0F).scaleY(1.0F)
                .setInterpolator(interpolator).alpha(1)
                .setDuration(mAnimationSpeed).setStartDelay(delay)
                .start();
    }

    private boolean hasServerProfileSetup() {
        JsServerProfile serverProfile = mJsRestClient.getServerProfile();
        boolean hasServerProfile = (serverProfile != null);
        if (!hasServerProfile) {
            ServersManagerActivity_.intent(this).startForResult(RC_SWITCH_SERVER_PROFILE);
            Toast.makeText(this, R.string.toast_select_profile, Toast.LENGTH_LONG).show();
        }
        return hasServerProfile;
    }

    private boolean hasNetwork() {
        boolean hasNetwork = mConnectivityUtil.isConnected();
        if (!hasNetwork) {
            AlertDialogFragment.createBuilder(this, getSupportFragmentManager())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.h_ad_title_no_connection)
                    .setMessage(R.string.h_ad_msg_no_connection)
                    .show();
        }
        return hasNetwork;
    }

    private void reloadProfileNameView() {
        JsServerProfile serverProfile = mJsRestClient.getServerProfile();
        if (serverProfile == null) {
            ServersManagerActivity_.intent(this).startForResult(RC_SWITCH_SERVER_PROFILE);
        } else {
            mProfileNameText.setText(serverProfile.getAlias());

            // savedInstanceState is null on first start, non-null on restart
            boolean isPasswordMissingOnFirstLaunch =
                    (mSavedInstanceState == null && TextUtils.isEmpty(serverProfile.getPassword()));

            if (isPasswordMissingOnFirstLaunch) {
                PasswordDialogFragment.show(getSupportFragmentManager());
            }
        }
    }

}