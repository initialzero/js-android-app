package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.accounts.Account;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPageFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.BaseActionBarActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsFragment_;
import com.jaspersoft.android.jaspermobile.widget.NavigationPanelLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @author Tom Koptel
 * @author Andrew Tivodar
 * @since 1.0
 */
@EActivity(R.layout.activity_navigation)
public class NavigationActivity extends BaseActionBarActivity {
    private static final String DIRECT_TAG = "DIRECT_FRAGMENT";

    @ViewById(R.id.tb_navigation)
    Toolbar drawerToolbar;
    @ViewById(R.id.dl_navigation)
    DrawerLayout drawerLayout;
    @ViewById(R.id.npl_navigation_menu)
    NavigationPanelLayout navigationPanelLayout;

    @Extra
    protected int defaultSelection = R.id.vg_library;

    private ActionBarDrawerToggle mDrawerToggle;
    private Bundle mSavedInstanceState;

    private boolean shouldGoInvisible;
    private float mPreviousOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @AfterViews
    final void init() {
        if (drawerToolbar != null) {
            setSupportActionBar(drawerToolbar);
            getSupportActionBar().setTitle(R.string.app_label);
        }
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, drawerToolbar, R.string.nd_drawer_open, R.string.nd_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                shouldGoInvisible = false;
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                shouldGoInvisible = true;
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View arg0, float slideOffset) {
                super.onDrawerSlide(arg0, slideOffset);
                if(slideOffset > mPreviousOffset && !shouldGoInvisible){
                    shouldGoInvisible = true;
                    invalidateOptionsMenu();
                }else if(mPreviousOffset > slideOffset && slideOffset < 0.5f && shouldGoInvisible){
                    shouldGoInvisible = false;
                    invalidateOptionsMenu();
                }
                mPreviousOffset = slideOffset;
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);

        navigationPanelLayout.setListener(new NavigationPanelLayout.NavigationListener() {
            @Override
            public void onNavigate(int viewId) {
                replaceContent(viewId);
                drawerLayout.closeDrawer(navigationPanelLayout);
            }

            @Override
            public void onProfileChange(Account account) {

            }
        });

        if (mSavedInstanceState == null) {
            replaceContent(defaultSelection);
            navigationPanelLayout.setItemSelected(defaultSelection);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = shouldGoInvisible;
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void replaceContent(int viewId) {
        Fragment directFragment = null;

        switch (viewId) {
            case R.id.vg_library:
                directFragment = LibraryFragment_.builder().build();
                break;
            case R.id.vg_repository:
                directFragment = RepositoryFragment_.builder().build();
                break;
            case R.id.vg_saved_items:
                directFragment = SavedReportsFragment_.builder().build();
                break;
            case R.id.vg_favorites:
                directFragment = FavoritesPageFragment_.builder().build();
                break;
            case R.id.vg_settings:
                SettingsActivity_.intent(this).start();
                break;
            default:
                return;
        }

        if (directFragment != null) {
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
                    .replace(R.id.main_frame, directFragment, DIRECT_TAG)
                    .commit();
        }
    }

    private void hideMenuItems(Menu menu, boolean visible) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(visible);
        }
    }
}
