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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.widget.ProgressBar;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardCordovaWebClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.CordovaInterfaceImpl;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.SessionListener;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.SimpleChromeClient;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.bridge.ReportCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.webview.bridge.ReportWebInterface;
import com.jaspersoft.android.jaspermobile.dialog.LogDialog;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

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
import org.apache.commons.io.IOUtils;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.Whitelist;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@OptionsMenu(R.menu.retrofit_report_menu)
@EActivity(R.layout.activity_cordova_dashboard_viewer)
public class ReportViewerActivity extends RoboToolbarActivity implements ReportCallback {

    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    protected FavoritesHelper favoritesHelper;

    @ViewById
    protected CordovaWebView webView;
    @ViewById
    protected ProgressBar progressBar;

    @Extra
    protected ResourceLookup resource;

    @InstanceState
    protected Uri favoriteEntryUri;

    @OptionsMenuItem
    protected MenuItem favoriteAction;

    private SimpleChromeClient chromeClient;
    private AccountServerData accountServerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scrollableTitleHelper.injectTitle(resource.getLabel());

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }

        Account account = JasperAccountManager.get(this).getActiveAccount();
        accountServerData = AccountServerData.get(this, account);
    }

    @AfterViews
    final void init() {
        setupSettings();
        setupJsInterface();
        initCordovaWebView();
        loadFlow();
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.webView != null) {
            webView.handleDestroy();
        }
    }

    @OptionsItem
    final void showLog() {
        if (chromeClient != null) {
            LogDialog.create(getSupportFragmentManager(), chromeClient.getMessages());
        }
    }

    //---------------------------------------------------------------------
    // Javascript callbacks
    //---------------------------------------------------------------------

    @UiThread
    @Override
    public void onScriptLoaded() {
        String organization = TextUtils.isEmpty(accountServerData.getOrganization()) ? "" : accountServerData.getOrganization();
        String executeScript = String.format("javascript:MobileReport.run({ \"uri\": \"%s\", \"username\": \"%s\", \"password\": \"%s\", \"organization\": \"%s\" })",
                resource.getUri(),
                accountServerData.getUsername(),
                accountServerData.getPassword(),
                organization);
        webView.loadUrl(executeScript);
    }

    @UiThread
    @Override
    public void onLoadStart() {
        ProgressDialogFragment.builder(getSupportFragmentManager()).show();
    }

    @UiThread
    @Override
    public void onLoadDone(String parameters) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @UiThread
    @Override
    public void onLoadError(String error) {

    }

    @UiThread
    @Override
    public void onTotalPagesLoaded(int pages) {

    }

    @UiThread
    @Override
    public void onPageChange(int page) {

    }

    @UiThread
    @Override
    public void onRemoteCall(String type, String location) {

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

    @SuppressLint({"AddJavascriptInterface"})
    private void setupJsInterface() {
        webView.addJavascriptInterface(new ReportWebInterface(this), "Android");
    }

    private void initCordovaWebView() {
        Whitelist whitelist = new Whitelist();
        whitelist.addWhiteListEntry("http://*/*", true);
        whitelist.addWhiteListEntry("https://*/*", true);
        CordovaPreferences cordovaPreferences = new CordovaPreferences();

        jsWebViewClient.setSessionListener(new SessionListener(this));

        CordovaInterface cordovaInterface = new CordovaInterfaceImpl(this);
        CordovaWebViewClient webViewClient2 = new DashboardCordovaWebClient(cordovaInterface, webView, jsWebViewClient);

        chromeClient = new SimpleChromeClient(cordovaInterface, webView, progressBar);

        List<PluginEntry> pluginEntries = (List<PluginEntry>) Collections.EMPTY_LIST;

        webView.init(cordovaInterface, webViewClient2, chromeClient, pluginEntries, whitelist, whitelist, cordovaPreferences);
    }

    private void loadFlow() {
        InputStream stream = null;
        try {
            stream = getAssets().open("report.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");

            Map<String, String> data = new HashMap<String, String>();
            data.put("visualize_url", accountServerData.getServerUrl() + "/client/visualize.js?_opt=true");
            Template tmpl = Mustache.compiler().compile(writer.toString());
            String html = tmpl.execute(data);

            webView.loadDataWithBaseURL(accountServerData.getServerUrl(), html, "text/html", "utf-8", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

}
