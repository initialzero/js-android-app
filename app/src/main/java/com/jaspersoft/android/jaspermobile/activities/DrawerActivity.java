package com.jaspersoft.android.jaspermobile.activities;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.account.AccountsFragment_;
import com.jaspersoft.android.jaspermobile.activities.favorites.FavoritesPageFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryFragment_;
import com.jaspersoft.android.jaspermobile.activities.repository.RepositoryFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity_;
import com.jaspersoft.android.jaspermobile.activities.storage.SavedReportsFragment_;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * SAMPLE ACTIVITY
 */
@EActivity(R.layout.drawer_layout)
public class DrawerActivity extends RoboSpiceFragmentActivity {
    private static final String DIRECT_TAG = "DIRECT_FRAGMENT";
    private String[] items;

    @ViewById
    protected ListView navList;
    @ViewById
    protected DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mAdapter;

    @Extra
    @InstanceState
    protected int position = Position.LIBRARY.ordinal();

    private float mPreviousOffset = 0;
    private boolean shouldGoInvisible;
    private Bundle mSavedInstanceState;

    public static enum Position {
        LIBRARY, REPOSITORY, SAVED_ITEMS, FAVORITES, SETTINGS, ACCOUNTS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @AfterViews
    final void init() {
        items = new String[]{
                getString(R.string.h_library_label),
                getString(R.string.h_repository_label),
                getString(R.string.sdr_ab_title),
                getString(R.string.f_title),
                getString(R.string.st_title),
                getString(R.string.accounts_activity_label),
        };

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        JsRestClient.flushCookies();
        setupNavigation();

        if (mSavedInstanceState == null) {
            onNavItemSelected(position);
        }
    }

    private void setupNavigation() {
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, items);
        navList.setAdapter(mAdapter);
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onNavItemSelected(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
                R.string.dummy_drawer_open, R.string.dummy_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = shouldGoInvisible;
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private void onNavItemSelected(int newPosition) {
        position = newPosition;
        String item = mAdapter.getItem(newPosition);

        navList.setItemChecked(newPosition, true);
        drawerLayout.closeDrawer(navList);

        Fragment directFragment = null;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (item.equals(items[0])) {
            directFragment = LibraryFragment_.builder().build();
        }
        if (item.equals(items[1])) {
            directFragment = RepositoryFragment_.builder().build();
        }
        if (item.equals(items[2])) {
            directFragment = SavedReportsFragment_.builder().build();
        }
        if (item.equals(items[3])) {
            directFragment = FavoritesPageFragment_.builder().build();
        }
        if (item.equals(items[4])) {
            SettingsActivity_.intent(this).start();
        }
        if (item.equals(items[5])) {
            directFragment = AccountsFragment_.builder().build();
        }

        if (directFragment != null) {
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
