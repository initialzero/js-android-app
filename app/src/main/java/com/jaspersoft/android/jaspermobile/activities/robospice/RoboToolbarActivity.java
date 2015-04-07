package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;

import org.androidannotations.api.ViewServer;
import org.roboguice.shaded.goole.common.collect.Lists;

import java.util.Locale;

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

    private Toolbar toolbar;
    private View baseView;
    private ViewGroup contentLayout;

    private JasperAccountManager mJasperAccountManager;
    JasperAccountsStatus mJasperAccountsStatus = JasperAccountsStatus.NO_CHANGES;

    private boolean windowToolbar;
    private Locale currentLocale;

    private final OnAccountsUpdateListener accountsUpdateListener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(Account[] allAccounts) {
            Timber.d("Accounts list was changed...");
            mJasperAccountsStatus = JasperAccountsStatus.ANY_ACCOUNT_CHANGED;
            defineJasperAccountsState();
        }
    };


    public boolean isDevMode() {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("dev");
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Lets check account to be properly setup
        mJasperAccountManager = JasperAccountManager.get(this);
        defineJasperAccountsState();
        updateActiveAccount();
        handleActiveAccountState();

        super.onCreate(savedInstanceState);
        addToolbar();
        Timber.tag(TAG);

        // Setup crash tracker
        BugSenseWrapper.initAndStartSession(this);

        // Setup initial locale
        currentLocale = Locale.getDefault();

        // Listen for view render events during dev process
        if (isDevMode()) {
            ViewServer.get(this).addWindow(this);
        }

        // Listen to account changes
        mJasperAccountManager.setOnAccountsUpdatedListener(accountsUpdateListener);
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
    protected void onStart() {
        updateActiveAccount();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDevMode()) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        updateAccountDependentUi();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void addToolbar() {
        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.windowToolbar});
        windowToolbar = a.getBoolean(0, true);
        if (!windowToolbar) return;

        LayoutInflater li = LayoutInflater.from(this);
        baseView = li.inflate(R.layout.view_base_toolbox_layout, null, false);
        contentLayout = (ViewGroup) baseView.findViewById(R.id.content);
        toolbar = (Toolbar) baseView.findViewById(R.id.tb_navigation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.setContentView(baseView);
    }

    private void defineJasperAccountsState(){
        Account[] accounts = mJasperAccountManager.getAccounts();
        Account currentAccount = mJasperAccountManager.getActiveAccount();

        if (accounts.length == 0) {
            mJasperAccountsStatus = JasperAccountsStatus.NO_ACCOUNTS;
        } else if (currentAccount != null) {
            boolean activeAccountExists = Lists.newArrayList(accounts).contains(currentAccount);
            if (!activeAccountExists) {
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

    private void restartApp(){
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    protected void onActiveAccountChanged() {
        restartApp();
    }

    protected void onAccountsChanged() {
    }

    public enum JasperAccountsStatus {
        NO_CHANGES, ANY_ACCOUNT_CHANGED, NO_ACTIVE_ACCOUNT, ACTIVE_ACCOUNT_CHANGED, NO_ACCOUNTS
    }
}
