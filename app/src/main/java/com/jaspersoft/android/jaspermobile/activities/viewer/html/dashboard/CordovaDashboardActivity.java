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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardCordovaWebClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.JsInjectorFactory;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.flow.WebFlowFactory;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.script.ScriptTagFactory;
import com.jaspersoft.android.jaspermobile.dialog.LogDialog;
import com.jaspersoft.android.jaspermobile.dialog.SimpleDialogFragment;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
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

/**
 * Activity that performs dashboard viewing in HTML format through Cordova native component.
 *
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity(R.layout.activity_cordova_dashboard_viewer)
@OptionsMenu(R.menu.dashboard_menu)
public class CordovaDashboardActivity extends RoboToolbarActivity implements CordovaInterface, DashboardCallback {
    private static final String FLOW_URI = "/dashboard/viewer.html?_opt=true&sessionDecorator=no&decorate=no#";

    @OptionsMenuItem
    protected MenuItem favoriteAction;

    @ViewById(R.id.cordova)
    protected CordovaWebView webView;
    @ViewById
    protected ProgressBar progressBar;

    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    protected FavoritesHelper favoritesHelper;

    @Extra
    protected ResourceLookup resource;

    @InstanceState
    protected boolean mMaximized;
    @InstanceState
    protected Uri favoriteEntryUri;

    private ExecutorService executorService;
    private ChromeClient chromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }
    }

    @AfterViews
    final void init() {
        setupSettings();
        setupJsInterface();
        initCordovaWebView();
        loadFlow();
    }

    @Override
    public void startActivityForResult(CordovaPlugin cordovaPlugin, Intent intent, int i) {
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin cordovaPlugin) {
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Object onMessage(String message, Object o) {
        if ("onPageFinished".equals(message)) {
            webView.loadUrl("javascript:" + ScriptTagFactory.getInstance(this).getTagCreator().createTag());
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        if (executorService == null) {
            executorService = new JobExecutor().getThreadPoolExecutor();
        }
        return executorService;
    }

    @Override
    public void onBackPressed() {
        if (mMaximized && webView != null) {
            webView.loadUrl("javascript:DashboardWrapper.minimizeDashlet()");
            scrollableTitleHelper.injectTitle("Test");
        } else {
            super.onBackPressed();
        }
    }

    @UiThread
    @Override
    public void onMaximize(String title) {
        mMaximized = true;
        scrollableTitleHelper.injectTitle(title);
    }

    @UiThread
    @Override
    public void onMinimize() {
        mMaximized = false;
    }

    @UiThread
    @Override
    public void onWrapperLoaded() {
        webView.loadUrl("javascript:DashboardWrapper.wrapScreen('100%', '100%')");
    }

    @Override
    public void onDashletsLoaded() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.webView != null) {
            webView.handleDestroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        favoriteAction.setIcon(favoriteEntryUri == null ? R.drawable.ic_star_outline : R.drawable.ic_star);
        favoriteAction.setTitle(favoriteEntryUri == null ? R.string.r_cm_add_to_favorites : R.string.r_cm_remove_from_favorites);

        if (BuildConfig.DEBUG) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.debug, menu);
        }

        return result;
    }

    @OptionsItem
    final void favoriteAction() {
        favoriteEntryUri = favoritesHelper.
                handleFavoriteMenuAction(favoriteEntryUri, resource, favoriteAction);
    }

    @OptionsItem
    final void refreshAction() {
        loadFlow();
    }

    @OptionsItem
    final void aboutAction() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(resource.getLabel())
                .setMessage(resource.getDescription())
                .setPositiveButtonText(android.R.string.ok)
                .show();
    }

    @OptionsItem
    final void showLog() {
        if (chromeClient != null) {
            LogDialog.create(getSupportFragmentManager(), chromeClient.messages);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

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

    private void setupJsInterface() {
        JsInjectorFactory.getInstance(this).createInjector()
                .inject(webView, new DashboardWebInterface(this));
    }

    private void initCordovaWebView() {
        Whitelist whitelist = new Whitelist();
        whitelist.addWhiteListEntry("http://*/*", true);
        whitelist.addWhiteListEntry("https://*/*", true);
        CordovaPreferences cordovaPreferences = new CordovaPreferences();

        WeakReference<Activity> reference = new WeakReference<Activity>(this);
        jsWebViewClient.setSessionListener(new SessionListener(reference));
        CordovaWebViewClient webViewClient2 = new DashboardCordovaWebClient(this, webView, jsWebViewClient);

        chromeClient = new ChromeClient(this, webView);

        List<PluginEntry> pluginEntries = (List<PluginEntry>) Collections.EMPTY_LIST;

        webView.init(this, webViewClient2, chromeClient, pluginEntries, whitelist, whitelist, cordovaPreferences);
    }

    private void loadFlow() {
        WebFlowFactory.getInstance(this).createFlow(resource).load(webView);
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private static class SessionListener implements JSWebViewClient.SessionListener {
        private final WeakReference<Activity> weakReference;

        private SessionListener(WeakReference<Activity> weakReference) {
            this.weakReference = weakReference;
        }

        @Override
        public void onSessionExpired() {
            if (weakReference.get() != null) {
                Toast.makeText(weakReference.get(), R.string.da_session_expired, Toast.LENGTH_LONG).show();
                weakReference.get().finish();
            }
        }
    }

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
