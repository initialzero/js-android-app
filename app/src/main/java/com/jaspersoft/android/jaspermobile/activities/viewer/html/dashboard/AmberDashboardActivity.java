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
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardCallback;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.DashboardWebInterface;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.bridge.JsInjectorFactory;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.flow.WebFlowFactory;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.script.ScriptTagFactory;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
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
public class AmberDashboardActivity extends DashboardCordovaActivity implements DashboardCallback {

    @Bean
    protected ScrollableTitleHelper scrollableTitleHelper;
    @Extra
    protected ResourceLookup resource;

    @InstanceState
    protected boolean mMaximized;

    private Toast mToast;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        scrollableTitleHelper.injectTitle(resource.getLabel());
        loadFlow();
    }

    @Override
    public void setupWebView(WebView webView) {
        JsInjectorFactory.getInstance(this).createInjector()
                .inject(webView, new DashboardWebInterface(this));
    }

    @Override
    public void onPageFinished() {
        webView.loadUrl("javascript:" + ScriptTagFactory.getInstance(this).getTagCreator().createTag());
    }

    @Override
    public void onRefresh() {
        loadFlow();
    }

    @Override
    public void onBackPressed() {
        if (mMaximized && webView != null) {
            webView.loadUrl("javascript:MobileDashboard.minimizeDashlet()");
            scrollableTitleHelper.injectTitle(resource.getLabel());
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
    public void onScriptLoaded() {
        webView.loadUrl("javascript:MobileDashboard.wrapScreen('100%', '100%')");
    }

    @UiThread
    @Override
    public void onLoadStart() {
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.da_loading).show();
    }

    @UiThread
    @Override
    public void onLoadDone() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @UiThread
    @Override
    public void onLoadError(String error) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
        mToast.setText(error);
        mToast.show();
    }

    private void loadFlow() {
        WebFlowFactory.getInstance(this).createFlow(resource).load(webView);
    }

}
