package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;

import org.androidannotations.api.ViewServer;

import java.util.Locale;

import roboguice.activity.RoboActionBarActivity;

/**
 * @author Andrew Tivodar
 * @author Tom Koptel
 * @since 2.0
 */
public class BaseActionBarActivity extends RoboActionBarActivity {

    private boolean mActiveAccountChanged;
    private boolean mAccountsChanged;

    private Locale currentLocale;
    private JasperAccountProvider mAccountProvider;

    private final OnAccountsUpdateListener accountsUpdateListener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(Account[] accounts) {
            mAccountsChanged = true;
            if (accounts.length == 0) {
                finish();
            } else {
                Account account = mAccountProvider.getAccount();
                boolean hasActiveAccount = Lists.newArrayList(accounts).contains(account);
                if (!hasActiveAccount) {
                    mAccountProvider.putAccount(accounts[0]);
                    mActiveAccountChanged = true;
                }
            }
        }
    };

    public boolean isDevMode() {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("dev");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup crash tracker
        BugSenseWrapper.initAndStartSession(this);

        // Setup initial locale
        currentLocale = Locale.getDefault();

        // Listen for view render events during dev process
        if (isDevMode()) {
            ViewServer.get(this).addWindow(this);
        }

        // Listen to account changes
        AccountManager.get(this).addOnAccountsUpdatedListener(accountsUpdateListener, null, true);
        mAccountProvider = JasperAccountProvider.get(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDevMode()) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        if (mActiveAccountChanged) {
            onActiveAccountChanged();
            mActiveAccountChanged = false;
        }
        if (mAccountsChanged) {
            onAccountsChanged();
            mAccountsChanged = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountManager.get(this).removeOnAccountsUpdatedListener(accountsUpdateListener);
        if (isDevMode()) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks change of localization
        // We are removing cookies as soon as they persist locale
        // New Basic Auth call we be triggered
        if (!currentLocale.equals(newConfig.locale)) {
            JasperMobileApplication.removeAllCookies();
            currentLocale = newConfig.locale;
        }
    }

    private void onActiveAccountChanged() {
        NavigationActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).start();
    }

    protected void onAccountsChanged() {
    }
}
