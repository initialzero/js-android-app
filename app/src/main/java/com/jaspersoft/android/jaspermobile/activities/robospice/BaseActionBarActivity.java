package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.common.collect.Lists;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.activities.intro.IntroPageActivity_;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountProvider;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;

import org.androidannotations.api.ViewServer;

import java.util.Locale;

import roboguice.activity.RoboActionBarActivity;
import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @author Tom Koptel
 * @since 2.0
 */
public class BaseActionBarActivity extends RoboActionBarActivity {

    private static final String TAG = BaseActionBarActivity.class.getSimpleName();
    private static final int AUTHORIZE_CODE = 10;

    private AccountManager mAccountManager;
    private AccountManagerUtil mAccountManagerUtil;
    private boolean mActiveAccountChanged;
    private boolean mAccountsChanged;

    private Locale currentLocale;
    private JasperAccountProvider mAccountProvider;

    private final OnAccountsUpdateListener accountsUpdateListener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(Account[] allAccounts) {
            Timber.d("accounts updated");
            mAccountsChanged = true;

            Account[] jasperAccounts = mAccountManagerUtil.getAccounts();
            if (jasperAccounts.length == 0) {
                Timber.d("No accounts");
                mAccountProvider.removeAccount();
            } else {
                Account currentAccount = mAccountProvider.getAccount();
                boolean activeAccountExists = Lists.newArrayList(jasperAccounts).contains(currentAccount);
                if (!activeAccountExists) {
                    Account newAccount = jasperAccounts[0];
                    Timber.d("Previous active account has been removed");
                    Timber.d("Previous " + currentAccount);
                    Timber.d("New " + newAccount);
                    mAccountProvider.putAccount(newAccount);
                    mActiveAccountChanged = true;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);

        // Setup crash tracker
        BugSenseWrapper.initAndStartSession(this);

        // Setup initial locale
        currentLocale = Locale.getDefault();

        // Listen for view render events during dev process
        if (isDevMode()) {
            ViewServer.get(this).addWindow(this);
        }

        boolean needInfo = DefaultPrefHelper_.getInstance_(this).needToShowIntro();
        if (needInfo) {
            IntroPageActivity_.intent(this).start();
        }

        // Listen to account changes
        mAccountProvider = JasperAccountProvider.get(this);
        mAccountManager = AccountManager.get(this);
        mAccountManagerUtil = AccountManagerUtil.get(this);

        mAccountManager.addOnAccountsUpdatedListener(accountsUpdateListener, null, true);

        // Lets check account to be properly setup
        assertJasperAccountState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHORIZE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onActiveAccountChanged();
            } else {
                finish();
            }
        }
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
            assertJasperAccountState();
            onAccountsChanged();
            mAccountsChanged = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAccountManager.removeOnAccountsUpdatedListener(accountsUpdateListener);
        if (isDevMode()) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    public boolean isDevMode() {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("dev");
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

    /**
     * Flow description can be found here <img src="http://code2flow.com/RXAXPt.png"/>
     */
    private void assertJasperAccountState() {
        // Get list of accounts on device.
        Account[] accounts = AccountManagerUtil.get(this).getAccounts();
        AccountProvider accountProvider = JasperAccountProvider.get(this);
        Account currentAccount = accountProvider.getAccount();

        if (accounts.length == 0) {
            Timber.d("We have found no accounts send user to account page.");
            if (currentAccount != null) {
                Timber.d("We have cached account. Removing...");
                accountProvider.removeAccount();
            }
            // Send the user to the "Add Account" page.
            startActivityForResult(new Intent(this, AuthenticatorActivity.class), AUTHORIZE_CODE);
        } else {

            if (currentAccount == null) {
                // Try to log the user in with the first account on the device.
                Account newAccount = accounts[0];
                Timber.d("We have found account activating...");
                Timber.d("New " + newAccount);
                accountProvider.putAccount(newAccount);
            } else {
                // Assert current account to really be present in system
                boolean activeAccountExists = Lists.newArrayList(accounts).contains(currentAccount);
                if (!activeAccountExists) {
                    Account newAccount = accounts[0];
                    Timber.d("Previous active account has been removed");
                    Timber.d("Previous " + currentAccount);
                    Timber.d("New " + newAccount);
                    mAccountProvider.putAccount(newAccount);
                    mActiveAccountChanged = true;
                }
            }
        }
    }

    protected void onActiveAccountChanged() {
    }

    protected void onAccountsChanged() {
    }
}
