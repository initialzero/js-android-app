/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardCordovaWebClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.SessionListener;
import com.jaspersoft.android.jaspermobile.dialog.LogDialog;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper_;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient_;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.Whitelist;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Activity that performs dashboard viewing in HTML format through Cordova native component.
 *
 * @author Tom Koptel
 * @since 2.0
 */
public abstract class DashboardCordovaActivity extends RoboToolbarActivity implements CordovaInterface {
    public final static String RESOURCE_EXTRA = "resource";


    protected CordovaWebView webView;
    private ProgressBar progressBar;
    private JSWebViewClient_ jsWebViewClient;
    private ChromeClient chromeClient;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    protected ResourceLookup resource;
    private MenuItem favoriteAction;

    private Uri favoriteEntryUri;
    private FavoritesHelper_ favoritesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cordova_dashboard_viewer);
        restoreSavedInstanceState(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            resource = extras.getParcelable(RESOURCE_EXTRA);
        }

        favoritesHelper = FavoritesHelper_.getInstance_(this);
        if (savedInstanceState == null && resource != null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }

        webView = (CordovaWebView) findViewById(R.id.cordova);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        jsWebViewClient = JSWebViewClient_.getInstance_(this);

        setupSettings();
        initCordovaWebView();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("favoriteEntryUri", favoriteEntryUri);
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        favoriteEntryUri = savedInstanceState.getParcelable("favoriteEntryUri");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu, menu);
        favoriteAction = menu.findItem(R.id.favoriteAction);

        favoriteAction.setIcon(favoriteEntryUri == null ? R.drawable.ic_star_outline : R.drawable.ic_star);
        favoriteAction.setTitle(favoriteEntryUri == null ? R.string.r_cm_add_to_favorites : R.string.r_cm_remove_from_favorites);

        if (BuildConfig.DEBUG) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.debug, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @java.lang.Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.refreshAction) {
            onRefresh();
            return true;
        }
        if (itemId_ == R.id.aboutAction) {
            aboutAction();
            return true;
        }
        if (itemId_ == R.id.favoriteAction) {
            favoriteAction();
            return true;
        }
        if (itemId_ == R.id.showLog) {
            showLog();
            return true;
        }
        return false;
    }

    private void favoriteAction() {
        favoriteEntryUri = favoritesHelper.
                handleFavoriteMenuAction(favoriteEntryUri, resource, favoriteAction);
    }


    private void aboutAction() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(resource.getLabel())
                .setMessage(resource.getDescription())
                .setNegativeButtonText(android.R.string.ok)
                .show();
    }

    private void showLog() {
        if (chromeClient != null) {
            LogDialog.create(getSupportFragmentManager(), chromeClient.messages);
        }
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {

    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Object onMessage(String message, Object data) {
        if ("onPageFinished".equals(message)) {
            onPageFinished();
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return executorService;
    }

    @Nullable
    public ChromeClient getChromeClient() {
        return chromeClient;
    }

    public abstract void onPageFinished();

    public abstract void onRefresh();

    public abstract void setupWebView(WebView webView);

    private void setupSettings() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        }
    }

    private void initCordovaWebView() {
        Whitelist whitelist = new Whitelist();
        whitelist.addWhiteListEntry("http://*/*", true);
        whitelist.addWhiteListEntry("https://*/*", true);
        CordovaPreferences cordovaPreferences = new CordovaPreferences();

        WeakReference<Activity> reference = new WeakReference<Activity>(this);
        jsWebViewClient.setSessionListener(new SessionListener(getActivity()));
        CordovaWebViewClient webViewClient2 = new DashboardCordovaWebClient(this, webView, jsWebViewClient);

        chromeClient = new ChromeClient(this, webView);

        List<PluginEntry> pluginEntries = (List<PluginEntry>) Collections.EMPTY_LIST;

        setupWebView(webView);
        webView.init(this, webViewClient2, chromeClient, pluginEntries, whitelist, whitelist, cordovaPreferences);
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class ChromeClient extends CordovaChromeClient {
        private final List<ConsoleMessage> messages = new LinkedList<ConsoleMessage>();

        public ChromeClient(CordovaInterface ctx, CordovaWebView app) {
            super(ctx, app);
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            int maxProgress = progressBar.getMax();
            progressBar.setProgress((maxProgress / 100) * progress);
            if (progress == maxProgress) {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            messages.add(consoleMessage);
            return super.onConsoleMessage(consoleMessage);
        }
    }
}
