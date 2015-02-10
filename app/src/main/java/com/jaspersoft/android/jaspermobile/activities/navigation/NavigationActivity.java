package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.auth.AuthenticatorActivity;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPageFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolboxActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsFragment_;
import com.jaspersoft.android.jaspermobile.widget.NavigationPanelLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @author Andrew Tivodar
 * @since 1.0
 */
@EActivity(R.layout.activity_navigation)
public class NavigationActivity extends RoboToolboxActivity {

    public static final String CURRENT_TAG = "CURRENT_FRAGMENT";
    private static final int NEW_ACCOUNT = 20;

    @ViewById(R.id.dl_navigation)
    protected DrawerLayout drawerLayout;
    @ViewById(R.id.npl_navigation_menu)
    protected NavigationPanelLayout navigationPanelLayout;

    @Extra
    protected int defaultSelection = R.id.vg_library;

    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mHideMenu;
    private float mPreviousOffset = 0f;

    @AfterViews
    final void setupNavigation() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_label);
        }
        setupNavDrawer();
        setupNavPanel();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            navigateToCurrentSelection();
        }
    }

    @Override
    protected void onAccountsChanged() {
        navigationPanelLayout.notifyAccountChange();
    }

    @Override
    protected void onActiveAccountChanged() {
        navigateToCurrentSelection();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(navigationPanelLayout);
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @OnActivityResult(NEW_ACCOUNT)
    final void newAccountAction(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            onActiveAccountChanged();
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void setupNavDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, getToolbar(),
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
        handleNavigationAction(defaultSelection);
        navigationPanelLayout.setItemSelected(defaultSelection);
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
            case R.id.vg_add_account:
                startActivityForResult(new Intent(this, AuthenticatorActivity.class), NEW_ACCOUNT);
                break;
            case R.id.vg_manage_accounts:
                String[] authorities = {"com.jaspersoft.android.jaspermobile.db.provider"};
                Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(Settings.EXTRA_AUTHORITIES, authorities);
                addExtraTypes(intent);
                startActivity(intent);
                break;
            case R.id.tv_settings:
                SettingsActivity_.intent(this).start();
                break;
            case R.id.tv_feedback:
                new FeedBackDialogFragment().show(getSupportFragmentManager(), FeedBackDialogFragment.class.getSimpleName());
                break;
            case R.id.tv_about:
                new AboutDialogFragment().show(getSupportFragmentManager(), AboutDialogFragment.class.getSimpleName());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void addExtraTypes(Intent intent) {
        String[] types = {"com.jaspersoft"};
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, types);
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
