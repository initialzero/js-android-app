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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ExportOutputData;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ReportExportOutputLoader;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.legacy.JsServerProfileCompat;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.jaspermobile.widget.JSWebView;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.report_html_viewer)
@OptionsMenu(R.menu.webview_menu)
public class NodeWebViewFragment extends RoboSpiceFragment {
    public static final String TAG = NodeWebViewFragment.class.getSimpleName();

    @ViewById
    protected JSWebView webView;
    @ViewById(R.id.htmlViewer_webView_progressBar)
    protected ProgressBar progressBar;

    @FragmentArg
    String currentHtml;

    @InstanceState
    @FragmentArg
    double versionCode;
    @InstanceState
    @FragmentArg
    int page;
    @InstanceState
    @FragmentArg
    String requestId;
    @FragmentArg
    @InstanceState
    String executionId;

    @FragmentArg
    @InstanceState
    boolean outputFinal;

    @Inject
    protected JsRestClient jsRestClient;

    @Bean
    JSWebViewClient jsWebViewClient;

    public static int getPage(NodeWebViewFragment fragment) {
        Bundle args = fragment.getArguments();
        return args.getInt(NodeWebViewFragment_.PAGE_ARG);
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initWebView();
    }

    @OptionsItem
    final void refreshAction() {
        fetchReport();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
        webView = null;
    }

    public boolean needUpdate() {
        PaginationManagerFragment paginationManagerFragment = (PaginationManagerFragment)
                getFragmentManager().findFragmentByTag(PaginationManagerFragment.TAG);
        String currentRequestId = paginationManagerFragment.getRequestId();
        return (!getRequestId().equals(currentRequestId));
    }

    public void refreshForNewRequestId() {
        PaginationManagerFragment paginationManagerFragment = (PaginationManagerFragment)
                getFragmentManager().findFragmentByTag(PaginationManagerFragment.TAG);
        requestId = paginationManagerFragment.getRequestId();
        fetchReport();
    }

    public void loadFinalOutput() {
        if (!outputFinal) fetchReport();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void initWebView() {
        CookieManagerFactory.syncCookies(getActivity(), jsRestClient);
        prepareWebView();
        setWebViewClient();
        loadHtml(currentHtml);
    }

    private void loadHtml(String html) {
        Preconditions.checkNotNull(html);
        Preconditions.checkNotNull(webView);
        Preconditions.checkNotNull(jsRestClient);

        if (!html.equals(currentHtml)) {
            currentHtml = html;
        }
        String mime = "text/html";
        String encoding = "utf-8";
        JsServerProfileCompat.initLegacyJsRestClient(getActivity(), jsRestClient);
        webView.loadDataWithBaseURL(
                jsRestClient.getServerProfile().getServerUrl(),
                currentHtml, mime, encoding, null);
    }

    private void setWebViewClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // fade in
                if (progressBar.getAlpha() == 0) {
                    ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f)
                            .setDuration(500).start();
                }
                // update value
                int maxProgress = progressBar.getMax();
                progressBar.setProgress((maxProgress / 100) * progress);
                // fade out
                if (progress == maxProgress) {
                    ObjectAnimator.ofFloat(progressBar, "alpha", 1f, 0f)
                            .setDuration(1000).start();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    private void prepareWebView() {
        // disable hardware acceleration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // configure additional settings
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setWebViewClient(jsWebViewClient);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private void fetchReport() {
        ReportExportOutputLoader.builder()
                .setControlFragment(this)
                .setExecutionMode(RequestExecutor.Mode.VISIBLE)
                .setJSRestClient(jsRestClient)
                .setRequestId(requestId)
                .setVersionCode(versionCode)
                .setResultListener(new ExportResultListener())
                .create()
                .loadByPage(page);
    }

    private String getRequestId() {
        if (requestId == null) {
            Bundle args = getArguments();
            requestId = args.getString(NodeWebViewFragment_.REQUEST_ID_ARG);
        }
        return requestId;
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class ExportResultListener implements ReportExportOutputLoader.ResultListener {
        @Override
        public void onFailure() {
        }

        @Override
        public void onSuccess(ExportOutputData output) {
            executionId = output.getExecutionId();
            outputFinal = output.isFinal();
            loadHtml(output.getData());
        }
    }

}