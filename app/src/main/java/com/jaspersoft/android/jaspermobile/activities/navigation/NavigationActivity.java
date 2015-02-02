package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.PrivacyPolicyActivity_;
import com.jaspersoft.android.jaspermobile.activities.account.AccountsActivity_;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPageFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.BaseActionBarActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsFragment_;
import com.jaspersoft.android.jaspermobile.widget.NavigationPanelLayout;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountProvider;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import timber.log.Timber;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @author Andrew Tivodar
 * @since 1.0
 */
@EActivity(R.layout.activity_navigation)
public class NavigationActivity extends BaseActionBarActivity {

    private static final String TAG = NavigationActivity.class.getSimpleName();
    private static final int AUTHORIZE = 10;
    public static final String CURRENT_TAG = "CURRENT_FRAGMENT";

    @ViewById(R.id.tb_navigation)
    protected Toolbar drawerToolbar;
    @ViewById(R.id.dl_navigation)
    protected DrawerLayout drawerLayout;
    @ViewById(R.id.npl_navigation_menu)
    protected NavigationPanelLayout navigationPanelLayout;

    @Extra
    protected int defaultSelection = R.id.vg_library;

    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mHideMenu;
    private boolean mHasAccount;
    private float mPreviousOffset = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);

        // Lets check accounts to be properly setup
        signInOrCreateAnAccount();
    }

    @AfterViews
    final void setupNavigation() {
        setSupportActionBar(drawerToolbar);
        setupNavDrawer();
        setupNavPanel();
    }

    private void signInOrCreateAnAccount() {
        //Get list of accounts on device.
        Account[] accounts = AccountManagerUtil.get(this).getAccounts();
        mHasAccount = (accounts.length != 0);
        if (mHasAccount) {
            //Try to log the user in with the first account on the device.
            AccountProvider accountProvider = JasperAccountProvider.get(this);
            if (accountProvider.getAccount() == null) {
                accountProvider.putAccount(accounts[0]);
            }
        } else {
            //Send the user to the "Add Account" page.
            Intent intent = new Intent(this, AuthenticatorActivity.class);
            intent.putExtra("account_types", new String[]{"com.jaspersoft"});
            startActivityForResult(intent, AUTHORIZE);
        }
    }

    @OnActivityResult(AUTHORIZE)
    protected void onAuthorize(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            mHasAccount = true;
            navigateToCurrentSelection();
        } else {
            mHasAccount = false;
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        if (mHasAccount) {
            navigateToCurrentSelection();
        }
    }

    @Override
    protected void onAccountsChanged() {
        signInOrCreateAnAccount();
        navigationPanelLayout.notifyAccountChange();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mHideMenu;
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void setupNavDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, drawerToolbar,
                R.string.nd_drawer_open, R.string.nd_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mHideMenu = false;
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mHideMenu = true;
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View arg0, float slideOffset) {
                super.onDrawerSlide(arg0, slideOffset);
                if (slideOffset > mPreviousOffset && !mHideMenu) {
                    mHideMenu = true;
                    invalidateOptionsMenu();
                } else if (mPreviousOffset > slideOffset && slideOffset < 0.5f && mHideMenu) {
                    mHideMenu = false;
                    invalidateOptionsMenu();
                }
                mPreviousOffset = slideOffset;
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setupNavPanel() {
        navigationPanelLayout.setListener(new NavigationPanelLayout.NavigationListener() {
            @Override
            public void onNavigate(int viewId) {
                handleNavigationAction(viewId);
                drawerLayout.closeDrawer(navigationPanelLayout);
            }

            @Override
            public void onProfileChange(Account account) {
                activateAccount(account);
            }
        });
    }

    private void navigateToCurrentSelection() {
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) == null) {
            handleNavigationAction(defaultSelection);
            navigationPanelLayout.setItemSelected(defaultSelection);
        }
    }

    private void handleNavigationAction(int viewId) {
        switch (viewId) {
            case R.id.vg_library:
                defaultSelection = R.id.vg_library;
                commitContent(LibraryFragment_.builder().build());
                break;
            case R.id.vg_repository:
                defaultSelection = R.id.vg_repository;
                commitContent(RepositoryFragment_.builder().build());
                break;
            case R.id.vg_saved_items:
                defaultSelection = R.id.vg_saved_items;
                commitContent(SavedReportsFragment_.builder().build());
                break;
            case R.id.vg_favorites:
                defaultSelection = R.id.vg_favorites;
                commitContent(FavoritesPageFragment_.builder().build());
                break;
            case R.id.vg_manage_accounts:
                AccountsActivity_.intent(this).start();
                break;
            case R.id.tv_settings:
                SettingsActivity_.intent(this).start();
                break;
            case R.id.tv_privacy_policy:
                PrivacyPolicyActivity_.intent(this).start();
                break;
            case R.id.tv_feedback:
                new FeedBackDialogFragment().show(getSupportFragmentManager(), FeedBackDialogFragment.class.getSimpleName());
                break;
            case R.id.tv_about:
                new AboutDialogFragment().show(getSupportFragmentManager(), AboutDialogFragment.class.getSimpleName());
        }
    }

    private void activateAccount(@NonNull Account account) {
        ActivationDialogFragment activationDialogFragment = ActivationDialogFragment_.builder()
                .account(account).build();
        activationDialogFragment.setActivationListener(
                new ActivationDialogFragment.OnActivationListener() {
                    @Override
                    public void onAccountActivation(Fragment page) {
                        drawerLayout.closeDrawer(navigationPanelLayout);
                        navigationPanelLayout.notifyAccountChange();
                        commitContent(page);
                    }
                });
        activationDialogFragment.show(getSupportFragmentManager(), null);
    }

    private void commitContent(@NonNull Fragment directFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    transaction.remove(fragment);
                }
            }
        }
        transaction
                .replace(R.id.main_frame, directFragment, CURRENT_TAG)
                .commit();
    }

    private void hideMenuItems(Menu menu, boolean visible) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(visible);
        }
    }

}
