package com.jaspersoft.android.jaspermobile.activities.robospice;

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
import com.jaspersoft.android.jaspermobile.network.BugSenseWrapper;

import org.androidannotations.api.ViewServer;

import java.util.Locale;

import roboguice.activity.RoboActionBarActivity;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class RoboToolboxActivity extends RoboActionBarActivity {
    private Locale currentLocale;
    private boolean windowToolbar;

    private Toolbar toolbar;
    private View baseView;
    private ViewGroup contentLayout;

    public Toolbar getToolbar() {return toolbar;}

    public boolean isDevMode() {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("dev");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addToolbar();

        BugSenseWrapper.initAndStartSession(this);
        currentLocale = Locale.getDefault();
        if (isDevMode()) {
            ViewServer.get(this).addWindow(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDevMode()) {
            ViewServer.get(this).setFocusedWindow(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if(item.getItemId() == android.R.id.home)  {
            super.onBackPressed();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    private void addToolbar(){
        TypedArray a = getTheme().obtainStyledAttributes(new int[] {R.attr.windowToolbar});
        windowToolbar = a.getBoolean(0, true);
        if(!windowToolbar) return;

        LayoutInflater li = LayoutInflater.from(this);
        baseView = li.inflate(R.layout.view_base_toolbox_layout, null, false);
        contentLayout = (ViewGroup) baseView.findViewById(R.id.content);
        toolbar = (Toolbar) baseView.findViewById(R.id.tb_navigation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.setContentView(baseView);
    }
}
