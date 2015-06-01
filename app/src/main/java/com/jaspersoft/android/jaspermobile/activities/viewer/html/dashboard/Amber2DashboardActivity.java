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
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.jaspermobile.visualize.HyperlinkHelper;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.MobileDashboardApi;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity
public class Amber2DashboardActivity extends BaseDashboardActivity implements DashboardCallback {
    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    protected HyperlinkHelper hyperlinkHelper;
    @Bean
    protected ScreenUtil screenUtil;
    @Extra
    protected ResourceLookup resource;

    @InstanceState
    protected boolean mMaximized;

    private boolean mFavoriteItemVisible, mInfoItemVisible;
    private MenuItem favoriteAction, aboutAction;
    private AccountServerData accountServerData;
    private Toast mToast;

    private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener(){
        @Override
        public void onCancel(DialogInterface dialog) {
            Amber2DashboardActivity.super.onBackPressed();
        }
    };

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        scrollableTitleHelper.injectTitle(resource.getLabel());

        Account account = JasperAccountManager.get(this).getActiveAccount();
        accountServerData = AccountServerData.get(this, account);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        favoriteAction = menu.findItem(R.id.favoriteAction);
        aboutAction = menu.findItem(R.id.aboutAction);
        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        favoriteAction.setVisible(mFavoriteItemVisible);
        aboutAction.setVisible(mInfoItemVisible);
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.loadUrl("javascript:MobileDashboard.pause()");
    }

    @Override
    protected void onResume() {
        super.onPause();
        webView.loadUrl("javascript:MobileDashboard.resume()");
    }

    //---------------------------------------------------------------------
    // Abstract methods implementations
    //---------------------------------------------------------------------

    @Override
    public void onWebViewConfigured(WebView webView) {
        WebViewEnvironment.configure(webView)
                .withWebInterface(DashboardWebInterface.from(this));
        loadFlow();
    }

    @UiThread
    @Override
    public void onMaximizeStart(String title) {
        resetZoom();
        hideMenuItems();
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .show();
    }

    @UiThread
    @Override
    public void onMaximizeEnd(String title) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        resetZoom();
        mMaximized = true;
        scrollableTitleHelper.injectTitle(title);
    }

    @UiThread
    @Override
    public void onMaximizeFailed(String error) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @UiThread
    @Override
    public void onMinimizeStart() {
        resetZoom();
        showMenuItems();
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .show();
    }

    @UiThread
    @Override
    public void onMinimizeEnd() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        mMaximized = false;
    }

    @UiThread
    @Override
    public void onMinimizeFailed(String error) {
    }

    @UiThread
    @Override
    public void onScriptLoaded() {
        runDashboard();
    }

    @UiThread
    @Override
    public void onLoadStart() {
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.da_loading)
                .setOnCancelListener(cancelListener)
                .show();
    }

    @UiThread
    @Override
    public void onLoadDone() {
        webView.setVisibility(View.VISIBLE);
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @UiThread
    @Override
    public void onLoadError(String error) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        mToast.setText(error);
        mToast.show();
    }

    @UiThread
    @Override
    public void onReportExecution(String data) {
        hyperlinkHelper.executeReport(data);
    }

    @Override
    public void onWindowResizeStart() {
    }

    @Override
    public void onWindowResizeEnd() {
    }

    @UiThread
    @Override
    public void onAuthError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        super.onSessionExpired();
    }

    @Override
    public void onPageFinished() {
    }

    @Override
    public void onRefresh() {
        if (mMaximized) {
            webView.loadUrl(MobileDashboardApi.refreshDashlet());
        } else {
            webView.loadUrl(MobileDashboardApi.refresh());
        }
    }

    @Override
    public void onHomeAsUpCalled() {
        if (mMaximized && webView != null) {
            webView.loadUrl(MobileDashboardApi.minimizeDashlet());
            scrollableTitleHelper.injectTitle(resource.getLabel());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSessionRefreshed() {
        loadFlow();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadFlow() {
        InputStream stream = null;
        try {
            stream = getAssets().open("dashboard.html");
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");

            Map<String, String> data = new HashMap<String, String>();
            data.put("visualize_url", accountServerData.getServerUrl() + "/client/visualize.js?_opt=true&_showInputControls=true");
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

    private void runDashboard() {
        StringBuilder builder = new StringBuilder();
        builder.append("javascript:MobileDashboard")
                .append(".configure({ \"diagonal\": %s })")
                .append(".run({ \"uri\": \"%s\" })");
        String executeScript = String.format(builder.toString(),
                screenUtil.getDiagonal(), resource.getUri());
        webView.loadUrl(executeScript);
    }

    private void showMenuItems() {
        mFavoriteItemVisible = mInfoItemVisible = true;
        supportInvalidateOptionsMenu();
    }

    private void hideMenuItems() {
        mFavoriteItemVisible = mInfoItemVisible = false;
        supportInvalidateOptionsMenu();
    }
}
