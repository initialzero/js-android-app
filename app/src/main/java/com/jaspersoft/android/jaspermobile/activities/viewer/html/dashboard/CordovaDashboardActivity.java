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

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolboxActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.DashboardWebClient2;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity(R.layout.cordova)
public class CordovaDashboardActivity extends RoboToolboxActivity implements CordovaInterface, DashboardCallback {
    private static final String FLOW_URI = "/dashboard/viewer.html?_opt=true&sessionDecorator=no&decorate=no#";

    @ViewById(R.id.cordova)
    protected CordovaWebView webView;
    @ViewById
    protected ProgressBar progressBar;

    @Bean
    protected JSWebViewClient jsWebViewClient;
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;

    @Extra
    protected ResourceLookup resource;

    @InstanceState
    protected boolean mMaximized;

    private ExecutorService executorService;

    @AfterViews
    final void init() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(true);

        Whitelist whitelist = new Whitelist();
        whitelist.addWhiteListEntry("http://*/*", true);
        whitelist.addWhiteListEntry("https://*/*", true);
        CordovaPreferences cordovaPreferences = new CordovaPreferences();
        CordovaWebViewClient webViewClient2 = new DashboardWebClient2(this, webView);
        CordovaChromeClient chromeClient = new ChromeClient(this, webView);
        List<PluginEntry> pluginEntries = Collections.EMPTY_LIST;

        webView.init(this, webViewClient2, chromeClient, pluginEntries, whitelist, whitelist, cordovaPreferences);
        webView.addJavascriptInterface(new DashboardWebInterface(this), "Android");

        Account account = JasperAccountManager.get(this).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(this, account);
        BasicAccessTokenEncoder tokenEncoder = BasicAccessTokenEncoder.builder()
                .setUsername(serverData.getUsername())
                .setOrganization(serverData.getOrganization())
                .setPassword(serverData.getPassword())
                .build();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Authorization",  tokenEncoder.encodeToken());

        String url = serverData.getServerUrl() + FLOW_URI + resource.getUri();
        webView.loadUrl(url, map);
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
            StringBuilder jsBuilder = new StringBuilder()
                    .append("var head= document.getElementsByTagName('head')[0];")
                    .append("var script= document.createElement('script');")
                    .append("script.type= 'text/javascript';")
                    .append("script.src= '"+ DashboardWebClient2.CLIENT_SCRIPT_SRC + "';")
                    .append("head.appendChild(script)");
            webView.loadUrl("javascript:" + jsBuilder.toString());
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

    private class ChromeClient extends CordovaChromeClient {
        public ChromeClient(CordovaInterface ctx, CordovaWebView app) {
            super(ctx, app);
        }

        public void onProgressChanged(WebView view, int progress) {
            int maxProgress = progressBar.getMax();
            progressBar.setProgress((maxProgress / 100) * progress);
            if (progress == maxProgress) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
