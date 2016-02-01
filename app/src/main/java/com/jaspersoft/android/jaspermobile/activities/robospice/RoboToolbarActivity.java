/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SecurityProviderUpdater;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ActiveProfileCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.PreferencesActiveProfileCache;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ProfileModule;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.util.ActivitySecureDelegate;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

import org.androidannotations.api.ViewServer;

import roboguice.RoboGuice;
import roboguice.activity.RoboActionBarActivity;
import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @author Tom Koptel
 * @since 2.0
 */
public class RoboToolbarActivity extends RoboActionBarActivity {

    private static final String TAG = RoboToolbarActivity.class.getSimpleName();
    private static final int AUTHORIZE_CODE = 10;
    private static final int SECURITY_PROVIDER_DIALOG_REQUEST_CODE = 1123;
    private static final String CLOSE_APP_REQUEST_CODE = "close_app";

    private Toolbar toolbar;
    private FrameLayout toolbarCustomView;
    private View baseView;
    private ViewGroup contentLayout;

    private boolean mSecureProviderDialogShown;
    private JasperAccountManager mJasperAccountManager;
    private JasperAccountsStatus mJasperAccountsStatus = JasperAccountsStatus.NO_CHANGES;

    private boolean windowToolbar;

    @Inject
    protected SecurityProviderUpdater mSecurityProviderUpdater;
    @Inject
    protected Analytics analytics;

    private final OnAccountsUpdateListener accountsUpdateListener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(Account[] allAccounts) {
            Timber.d("Accounts list was changed...");
            mJasperAccountsStatus = JasperAccountsStatus.ANY_ACCOUNT_CHANGED;
            defineJasperAccountsState();
            updateActiveAccount();
        }
    };

    public boolean isDevMode() {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("dev");
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Set custom view instead of toolbar title. Can be used only after activity is created.
     *
     * @param view custom view for Toolbar. Pass null to show default toolbar.
     */
    public void setCustomToolbarView(View view) {
        if (toolbarCustomView == null) return;

        toolbarCustomView.removeAllViews();
        getSupportActionBar().setDisplayShowTitleEnabled(view == null);
        if (view != null) {
            toolbarCustomView.addView(view);
        }
    }

    /**
     * Set whether a custom toolbar view should be displayed, if set.
     * If false, action bar title will be shown.
     */
    public void setDisplayCustomToolbarEnable(boolean enabled) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(toolbarCustomView.getChildCount() == 0 || !enabled);
        }

        for (int i = 0; i < toolbarCustomView.getChildCount(); i++) {
            toolbarCustomView.getChildAt(i).setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RoboGuice.getInjector(this).injectMembersWithoutViews(this);

        // Close activity if flag CLOSE_APP_REQUEST_CODE is active
        if (getIntent().getBooleanExtra(CLOSE_APP_REQUEST_CODE, false)) {
            finish();
        }

        setScreenName();

        // Lets update Security provider
        mSecurityProviderUpdater.update(this, new ProviderInstallListener());

        // Lets check account to be properly setup
        mJasperAccountManager = JasperAccountManager.get(this);
        defineJasperAccountsState();
        updateActiveAccount();
        handleActiveAccountState();
        disableScreenCapturing();

        super.onCreate(savedInstanceState);
        addToolbar();
        Timber.tag(TAG);

        // Listen for view render events during dev process
        if (isDevMode()) {
            ViewServer.get(this).addWindow(this);
        }

        // Listen to account changes
        mJasperAccountManager.setOnAccountsUpdatedListener(accountsUpdateListener);

        if (JasperMobileApplication.get(this).getProfileComponent() == null) {
            ActiveProfileCache activeProfileCache = new PreferencesActiveProfileCache(this);
            Profile profile = activeProfileCache.get();
            if (profile != null) {
                setupProfileModule(profile);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHORIZE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String profileName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Profile profile = Profile.create(profileName);

                setupProfileModule(profile);

                onActiveAccountChanged();
            } else {
                finish();
            }
        } else if (requestCode == SECURITY_PROVIDER_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            mSecureProviderDialogShown = true;
        }
    }

    private void setupProfileModule(Profile profile) {
        GraphObject graphObject = JasperMobileApplication.get(this);
        ProfileComponent profileComponent = graphObject.getComponent()
                .plus(new ProfileModule(profile));
        graphObject.setProfileComponent(profileComponent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDevMode()) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        updateAccountDependentUi();

        trackScreenView();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mSecureProviderDialogShown) {
            // We can now safely retry Security provider installation.
            mSecurityProviderUpdater.update(this, new ProviderInstallListener());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mJasperAccountManager.removeOnAccountsUpdatedListener(accountsUpdateListener);
        if (isDevMode()) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    @Override
    public void setContentView(View view) {
        if (!windowToolbar) {
            super.setContentView(view);
            return;
        }

        contentLayout.removeAllViews();
        contentLayout.addView(view);
        super.setContentView(baseView);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (!windowToolbar) {
            super.setContentView(layoutResID);
            return;
        }

        contentLayout.removeAllViews();
        LayoutInflater.from(this).inflate(layoutResID, contentLayout, true);
        super.setContentView(baseView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void disableScreenCapturing(){
        boolean isScreenCaptureEnable = DefaultPrefHelper_.getInstance_(this).isScreenCapturingEnabled();

        if (!isScreenCaptureEnable) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    private void addToolbar() {
        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.windowToolbar});
        windowToolbar = a.getBoolean(0, true);
        if (!windowToolbar) return;

        LayoutInflater li = LayoutInflater.from(this);
        baseView = li.inflate(R.layout.view_base_toolbox_layout, null, false);
        contentLayout = (ViewGroup) baseView.findViewById(R.id.content);
        toolbar = (Toolbar) baseView.findViewById(R.id.tb_navigation);
        toolbarCustomView = (FrameLayout) toolbar.findViewById(R.id.tb_custom);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.setContentView(baseView);
    }

    private void defineJasperAccountsState() {
        Account[] accounts = mJasperAccountManager.getAccounts();
        Account currentAccount = mJasperAccountManager.getActiveAccount();

        if (accounts.length == 0) {
            mJasperAccountsStatus = JasperAccountsStatus.NO_ACCOUNTS;
        } else if (currentAccount != null) {
            if (!mJasperAccountManager.isActiveAccountRegistered()) {
                mJasperAccountsStatus = JasperAccountsStatus.ACTIVE_ACCOUNT_CHANGED;
            }
        } else {
            mJasperAccountsStatus = JasperAccountsStatus.NO_ACTIVE_ACCOUNT;
        }
    }

    private void updateActiveAccount() {
        if (mJasperAccountsStatus == JasperAccountsStatus.NO_CHANGES)
            return;

        switch (mJasperAccountsStatus) {
            case NO_ACCOUNTS:
                Timber.d("We have found no accounts. send user to account page.");
                Timber.d("Try to remove active account...");
                mJasperAccountManager.deactivateAccount();
                break;

            case ACTIVE_ACCOUNT_CHANGED:
                Timber.d("Previous active account has been removed");
                Timber.d("Try to activate first account");
                mJasperAccountManager.activateFirstAccount();
                break;

            case NO_ACTIVE_ACCOUNT:
                Timber.d("Try to activate first account");
                mJasperAccountManager.activateFirstAccount();
                break;
        }
    }

    private void handleActiveAccountState() {
        if (mJasperAccountsStatus == JasperAccountsStatus.NO_CHANGES)
            return;

        switch (mJasperAccountsStatus) {
            case NO_ACCOUNTS:
                Timber.d("Send user to account page.");
                startActivityForResult(new Intent(this, AuthenticatorActivity.class), AUTHORIZE_CODE);
                break;
        }
        mJasperAccountsStatus = JasperAccountsStatus.NO_CHANGES;
    }

    /**
     * Flow description can be found here <img src="http://code2flow.com/RXAXPt.png"/>
     */
    private void updateAccountDependentUi() {
        if (mJasperAccountsStatus == JasperAccountsStatus.NO_CHANGES)
            return;

        switch (mJasperAccountsStatus) {
            case NO_ACCOUNTS:
                restartApp();
                break;

            case ANY_ACCOUNT_CHANGED:
                onAccountsChanged();
                break;

            case ACTIVE_ACCOUNT_CHANGED:
                onActiveAccountChanged();
                onAccountsChanged();
                break;
        }
        mJasperAccountsStatus = JasperAccountsStatus.NO_CHANGES;
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void closeApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(CLOSE_APP_REQUEST_CODE, true);
        startActivity(i);
    }

    protected void onActiveAccountChanged() {
        restartApp();
    }

    protected void onAccountsChanged() {
    }

    protected String getScreenName() {
        return null;
    }

    protected void setScreenName() {
        String screenName = getScreenName();
        if (screenName != null) {
            analytics.setScreenName(screenName);
        }
    }

    protected void trackScreenView() {
        String screenName = getScreenName();
        if (screenName != null) {
            analytics.sendScreenView(screenName, null);
        }
    }

    public enum JasperAccountsStatus {
        NO_CHANGES, ANY_ACCOUNT_CHANGED, NO_ACTIVE_ACCOUNT, ACTIVE_ACCOUNT_CHANGED, NO_ACCOUNTS
    }

    private class ProviderInstallListener implements ProviderInstaller.ProviderInstallListener {

        @Override
        public void onProviderInstalled() {
            // Provider is up-to-date, app can make secure network calls.
        }

        @Override
        public void onProviderInstallFailed(int errorCode, Intent intent) {
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            if (googleApiAvailability.isUserResolvableError(errorCode) && !mSecureProviderDialogShown) {
                // Recoverable error. Show a dialog prompting the user to
                // install/update/enable Google Play services.
                googleApiAvailability.showErrorDialogFragment(
                        RoboToolbarActivity.this,
                        errorCode,
                        SECURITY_PROVIDER_DIALOG_REQUEST_CODE,
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // The user chose not to take the recovery action
                                closeApp();
                            }
                        });
            } else {
                // Google Play services is not available.
                closeApp();
            }

        }
    }
}
