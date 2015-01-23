package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.BaseActionBarActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */

@OptionsMenu(R.menu.home_menu)
@EActivity(R.layout.activity_navigation)
public class NavigationActivity extends BaseActionBarActivity {

    @ViewById(R.id.dlNavigationAN)
    DrawerLayout mDrawerLayout;
    @ViewById(R.id.tbNavigationVNT)
    Toolbar mDrawerToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    @AfterViews
    final void init() {
        if (mDrawerToolbar != null) {
            mDrawerToolbar.setTitle("Navigation Drawer");
            setSupportActionBar(mDrawerToolbar);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mDrawerToolbar, R.string.nd_drawer_open, R.string.nd_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.string.nd_action_settings) {
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
