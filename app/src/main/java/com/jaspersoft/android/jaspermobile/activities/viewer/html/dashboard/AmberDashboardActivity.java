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

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.webview.WebViewEnvironment;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.webview.dashboard.bridge.MobileDashboardApi;
import com.jaspersoft.android.jaspermobile.webview.dashboard.flow.WebFlowFactory;
import com.jaspersoft.android.jaspermobile.webview.dashboard.script.ScriptTagFactory;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.util.ScreenUtil;
import com.jaspersoft.android.jaspermobile.util.ScrollableTitleHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EActivity
public class AmberDashboardActivity extends BaseDashboardActivity implements DashboardCallback {

    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Bean
    protected ScreenUtil screenUtil;

    @Extra
    protected ResourceLookup resource;

    @InstanceState
    protected boolean mMaximized;

    private Toast mToast;
    private int mOrientation;
    private boolean mFavoriteItemVisible, mRefreshItemVisible, mInfoItemVisible;
    private MenuItem favoriteAction, refreshAction, aboutAction;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        scrollableTitleHelper.injectTitle(resource.getLabel());
        loadFlow();
        showMenuItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        favoriteAction = menu.findItem(R.id.favoriteAction);
        refreshAction = menu.findItem(R.id.refreshAction);
        aboutAction = menu.findItem(R.id.aboutAction);
        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);
        favoriteAction.setVisible(mFavoriteItemVisible);
        refreshAction.setVisible(mRefreshItemVisible);
        aboutAction.setVisible(mInfoItemVisible);
        return result;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientation && mOrientation != -1) {
            mOrientation = newConfig.orientation;
            ProgressDialogFragment.builder(getSupportFragmentManager())
                    .setLoadingMessage(R.string.loading_msg)
                    .show();
        }
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
        WebViewEnvironment
                .configure(webView)
                .withWebInterface(DashboardWebInterface.from(this));
        showInitialLoader();
    }

    @Override
    public void onPageFinished() {
        webView.loadUrl(ScriptTagFactory.getInstance(this).getTagCreator().createTag());
    }

    @Override
    public void onRefresh() {
        loadFlow();
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

    //---------------------------------------------------------------------
    // DashboardCallback implementations
    //---------------------------------------------------------------------

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
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @UiThread
    @Override
    public void onScriptLoaded() {
        runDashboard();
    }

    @UiThread
    @Override
    public void onLoadStart() {
    }

    @UiThread
    @Override
    public void onLoadDone() {
        hideInitialLoader();
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
    }

    @UiThread
    @Override
    public void onWindowResizeStart() {
    }

    @UiThread
    @Override
    public void onWindowResizeEnd() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void loadFlow() {
        WebFlowFactory.getInstance(this).createFlow(resource).load(webView);
    }

    private void runDashboard() {
        String runScript = String.format(
                "javascript:MobileDashboard.configure({\"diagonal\": \"%s\"}).run()",
                screenUtil.getDiagonal());
        webView.loadUrl(runScript);
    }

    private void showInitialLoader() {
        webView.setVisibility(View.INVISIBLE);
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.da_loading).show();
    }

    private void hideInitialLoader() {
        webView.setVisibility(View.VISIBLE);
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    private void showMenuItems() {
        mFavoriteItemVisible = mRefreshItemVisible = mInfoItemVisible = true;
        supportInvalidateOptionsMenu();
    }

    private void hideMenuItems() {
        mFavoriteItemVisible = mRefreshItemVisible = mInfoItemVisible = false;
        supportInvalidateOptionsMenu();
    }
}
