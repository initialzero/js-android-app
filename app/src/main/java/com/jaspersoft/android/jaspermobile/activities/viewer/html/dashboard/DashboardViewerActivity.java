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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolboxActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardLegacyWebClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.flow.WebFlowFactory;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.flow.WebFlowStrategy;
import com.jaspersoft.android.jaspermobile.dialog.LogDialog;
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

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Activity that performs dashboard viewing in HTML format.
 *
 * @author Ivan Gadzhega
 * @since 1.4
 */
@EActivity(R.layout.activity_dashboard_viewer)
@OptionsMenu(R.menu.dashboard_menu)
public class DashboardViewerActivity extends RoboToolboxActivity implements DashboardCallback  {

    @OptionsMenuItem
    protected MenuItem favoriteAction;

    @Extra
    protected ResourceLookup resource;

    @Bean
    protected FavoritesHelper favoritesHelper;
    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;

    @InstanceState
    protected Uri favoriteEntryUri;
    @InstanceState
    protected boolean mMaximized;

    @ViewById
    protected WebView webView;
    @ViewById
    protected ProgressBar progressBar;

    private ChromeClient chromeClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }
    }

    @AfterViews
    final void init() {
        setupSettings();

        chromeClient = new ChromeClient();
        webView.setWebChromeClient(chromeClient);

        jsWebViewClient.setSessionListener(new SessionListener(new WeakReference<Activity>(this)));
        webView.setWebViewClient(new DashboardLegacyWebClient(jsWebViewClient));
        webView.addJavascriptInterface(new DashboardWebInterface(this), "Android");
        webView.setInitialScale(2);

        WebFlowFactory.getInstance(this).createFlow(resource).load(webView);
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
        WebFlowStrategy webFlow = WebFlowFactory.getInstance(this).createFlow(resource);
        webFlow.load(webView);
    }

    @OptionsItem
    final void aboutAction() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(resource.getLabel())
                .setMessage(resource.getDescription())
                .setNegativeButtonText(android.R.string.ok)
                .show();
    }

    @OptionsItem
    final void showLog() {
        if (chromeClient != null) {
            LogDialog.create(getSupportFragmentManager(), chromeClient.messages);
        }
    }

    private void setupSettings() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        }
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

    private class ChromeClient extends WebChromeClient {
        private final List<ConsoleMessage> messages = new LinkedList<ConsoleMessage>();

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
