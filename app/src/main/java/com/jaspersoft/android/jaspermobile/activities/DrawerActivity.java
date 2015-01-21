package com.jaspersoft.android.jaspermobile.activities;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
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
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

/**
 * SAMPLE ACTIVITY
 */
@EActivity(R.layout.drawer_layout)
public class DrawerActivity extends RoboSpiceFragmentActivity {
    private static final String DIRECT_TAG = "DIRECT_FRAGMENT";
    private static final String[] ITEMS = {"Library", "Repository", "Saved items", "Favorites", "Settings", "Accounts"};

    @ViewById
    protected ListView navList;
    @ViewById
    protected DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mAdapter;

    @InstanceState
    protected int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        JsRestClient.flushCookies();
        setupNavigation();
        onNavItemSelected(position);
    }

    @AfterViews
    final void init() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        JsRestClient.flushCookies();
        setupNavigation();
    }

    private void setupNavigation() {
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, ITEMS);
        navList.setAdapter(mAdapter);
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onNavItemSelected(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
                R.string.dummy_drawer_open, R.string.dummy_drawer_close) {
            public void onDrawerClosed(View view) {
            }

            public void onDrawerOpened(View drawerView) {
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
        getActionBar().setTitle(item);

        Fragment directFragment = null;
        if (item.equals(ITEMS[0])) {
            directFragment = LibraryFragment_.builder().build();
        }
        if (item.equals(ITEMS[1])) {
            directFragment = RepositoryFragment_.builder().build();
        }
        if (item.equals(ITEMS[2])) {
            directFragment = SavedReportsFragment_.builder().build();
        }
        if (item.equals(ITEMS[3])) {
            directFragment = FavoritesPageFragment_.builder().build();
        }
        if (item.equals(ITEMS[4])) {
            SettingsActivity_.intent(this).start();
        }
        if (item.equals(ITEMS[5])) {
            directFragment = AccountsFragment_.builder().build();
        }

        if (directFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, directFragment, DIRECT_TAG)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // Because of official issue in Andorid SDK https://code.google.com/p/android/issues/detail?id=40323
        // We need manage backstack artificially
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment frag : fm.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }
        super.onBackPressed();
    }
}
